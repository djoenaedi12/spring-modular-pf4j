const _ = require('lodash');
const pluralize = require('pluralize');
const path = require('path');
const fs = require('fs-extra');
const chalk = require('chalk');
const ora = require('ora');
const inquirer = require('inquirer');

const { resolveCwd, assertProjectRoot, getPluginModules, detectPluginFromCwd } = require('../plugin-utils');
const { validateEntityName, validateFieldName, validateEnumName } = require('../validators');
const { generateResource } = require('../resource-generator');
const { normalizeRenderedContent } = require('../template-engine');

/**
 * Field‐type choices shown in the interactive prompt.
 *
 * "value" is the canonical key used by the generator;
 * the mapping to a concrete Java class lives in resource-templates.js
 */
const FIELD_TYPES = [
    { name: 'String', value: 'String' },
    { name: 'Text', value: 'Text' },
    { name: 'MediumText', value: 'MediumText' },

    { name: 'Integer', value: 'Integer' },
    { name: 'Long', value: 'Long' },
    { name: 'BigDecimal', value: 'BigDecimal' },
    { name: 'Double', value: 'Double' },
    { name: 'Boolean', value: 'Boolean' },
    { name: 'Date', value: 'Date' },
    { name: 'DateTime', value: 'DateTime' },
    { name: 'Instant', value: 'Instant' },
    { name: 'Enum', value: 'Enum' },
    { name: 'ManyToOne', value: 'ManyToOne' },
];

const FIELD_TYPE_VALUES = new Set(FIELD_TYPES.map((t) => t.value));
const DTO_KEYS = ['create', 'update', 'summary', 'detail'];
const STRING_TYPES = new Set(['String', 'Text', 'MediumText']);
const INTEGER_TYPES = new Set(['Integer', 'Long']);
const DECIMAL_TYPES = new Set(['BigDecimal', 'Double']);
const DATE_TYPES = new Set(['Date', 'DateTime', 'Instant']);

async function resolvePluginMetadata(pluginDir) {
    const propsFile = path.join(pluginDir, 'src', 'main', 'resources', 'plugin.properties');

    if (!(await fs.pathExists(propsFile))) {
        return null;
    }

    const content = await fs.readFile(propsFile, 'utf8');

    const classMatch = content.match(/^plugin\.class[ \t]*=[ \t]*([^\r\n]*)/m);
    const prefixMatch = content.match(/^plugin\.prefix[ \t]*=[ \t]*([^\r\n]*)/m);

    let packageName = null;

    if (classMatch) {
        const fqcn = classMatch[1].trim();
        const lastDot = fqcn.lastIndexOf('.');

        if (lastDot > 0) {
            packageName = fqcn.substring(0, lastDot);
        }
    }

    return {
        packageName,
        pluginPrefix: prefixMatch ? prefixMatch[1].trim() : '',
        propsFile,
    };
}

async function loadResourceSpecFile(filePath, cwd) {
    const absFile = path.isAbsolute(filePath)
        ? filePath
        : path.join(cwd, filePath);

    if (!(await fs.pathExists(absFile))) {
        throw new Error(`Resource definition file not found: ${absFile}`);
    }

    let spec;
    try {
        spec = await fs.readJson(absFile);
    } catch (err) {
        throw new Error(`Invalid JSON file: ${absFile}. ${err.message}`);
    }

    return {
        file: absFile,
        spec,
    };
}

function normalizeResourceSpecDocument(spec, fallbackEntityName, sourceLabel) {
    if (Array.isArray(spec)) {
        if (spec.length === 0) {
            throw new Error(`${sourceLabel} must not be an empty array.`);
        }

        const resources = spec.map((item, index) => {
            return validateSingleResourceSpec(item, null, `${sourceLabel}[${index}]`);
        });

        assertUniqueEntityNames(resources, sourceLabel);
        return resources;
    }

    if (!spec || typeof spec !== 'object') {
        throw new Error(`${sourceLabel} must be a JSON object or array.`);
    }

    if (Array.isArray(spec.resources)) {
        if (spec.resources.length === 0) {
            throw new Error(`${sourceLabel}.resources must be a non-empty array.`);
        }

        const resources = spec.resources.map((item, index) => {
            return validateSingleResourceSpec(item, null, `${sourceLabel}.resources[${index}]`);
        });

        assertUniqueEntityNames(resources, sourceLabel);
        return resources;
    }

    return [
        validateSingleResourceSpec(spec, fallbackEntityName, sourceLabel),
    ];
}

function validateSingleResourceSpec(spec, fallbackEntityName, label) {
    if (!spec || typeof spec !== 'object' || Array.isArray(spec)) {
        throw new Error(`${label} must be a JSON object.`);
    }

    const finalEntityName = spec.entityName || fallbackEntityName;

    if (!finalEntityName) {
        throw new Error(`${label}.entityName is required.`);
    }

    const entityResult = validateEntityName(finalEntityName);
    if (entityResult !== true) {
        throw new Error(`${label}.entityName is invalid: ${entityResult}`);
    }

    if (!Array.isArray(spec.fields) || spec.fields.length === 0) {
        throw new Error(`${label}.fields must be a non-empty array.`);
    }

    const existingNames = new Set();
    const fields = [];

    for (let i = 0; i < spec.fields.length; i++) {
        const raw = spec.fields[i];
        const fieldLabel = `${label}.fields[${i}]`;

        if (!raw || typeof raw !== 'object' || Array.isArray(raw)) {
            throw new Error(`${fieldLabel} must be a JSON object.`);
        }

        const field = {
            name: raw.name,
            type: raw.type,
            required: raw.required !== undefined ? Boolean(raw.required) : true,
            unique: raw.unique !== undefined ? Boolean(raw.unique) : false,
            filterable: raw.filterable !== undefined ? Boolean(raw.filterable) : false,
        };

        const nameResult = validateFieldName(field.name);
        if (nameResult !== true) {
            throw new Error(`${fieldLabel}.name is invalid: ${nameResult}`);
        }

        if (existingNames.has(field.name)) {
            throw new Error(`${fieldLabel}.name is duplicated: ${field.name}`);
        }

        existingNames.add(field.name);

        if (!FIELD_TYPE_VALUES.has(field.type)) {
            throw new Error(`${fieldLabel}.type is invalid: ${field.type}. Allowed: ${Array.from(FIELD_TYPE_VALUES).join(', ')}`);
        }

        if (field.type === 'String') {
            const length = raw.length !== undefined ? parseInt(raw.length, 10) : 255;

            if (!Number.isInteger(length) || length <= 0) {
                throw new Error(`${fieldLabel}.length must be a positive number.`);
            }

            field.length = length;
        }

        if (field.type === 'ManyToOne') {
            if (!raw.refEntity) {
                throw new Error(`${fieldLabel}.refEntity is required for ManyToOne field.`);
            }

            const refResult = validateEntityName(raw.refEntity);
            if (refResult !== true) {
                throw new Error(`${fieldLabel}.refEntity is invalid: ${refResult}`);
            }

            field.refEntity = raw.refEntity;
        }

        if (field.type === 'Enum') {
            if (!raw.enumName) {
                throw new Error(`${fieldLabel}.enumName is required for Enum field.`);
            }

            const enumResult = validateEnumName(raw.enumName);
            if (enumResult !== true) {
                throw new Error(`${fieldLabel}.enumName is invalid: ${enumResult}`);
            }

            field.enumName = raw.enumName;
        }

        if (field.unique && !['String', 'Integer', 'Long'].includes(field.type)) {
            throw new Error(`${fieldLabel}.unique is only supported for String, Integer, or Long.`);
        }

        field.dto = normalizeDtoConfig(raw.dto, `${fieldLabel}.dto`);
        field.validation = normalizeValidationConfig(raw.validation, field, `${fieldLabel}.validation`);

        fields.push(field);
    }

    const parent = normalizeParentConfig(spec.parent, `${label}.parent`);
    const explicitAs = normalizeAsConfig(spec.as, `${label}.as`);
    const as = explicitAs || (parent ? pluralize(_.lowerFirst(finalEntityName)) : null);
    const exposeApi = spec.exposeApi !== undefined
        ? Boolean(spec.exposeApi)
        : parent ? false : true;

    if (parent) {
        const parentFieldName = _.lowerFirst(parent);
        const existingParentField = fields.find((field) => field.name === parentFieldName);

        if (existingParentField && (existingParentField.type !== 'ManyToOne' || existingParentField.refEntity !== parent)) {
            throw new Error(`${label}.parent conflicts with field "${parentFieldName}". Parent field must be ManyToOne to ${parent}.`);
        }
    }

    return {
        entityName: finalEntityName,
        parent,
        as,
        exposeApi,
        fields,
    };
}

function normalizeParentConfig(parent, label) {
    if (parent === undefined || parent === null || parent === '') {
        return null;
    }

    if (typeof parent !== 'string') {
        throw new Error(`${label} must be a string.`);
    }

    const trimmed = parent.trim();
    const result = validateEntityName(trimmed);
    if (result !== true) {
        throw new Error(`${label} is invalid: ${result}`);
    }

    return trimmed;
}

function normalizeAsConfig(as, label) {
    if (as === undefined || as === null || as === '') {
        return null;
    }

    if (typeof as !== 'string') {
        throw new Error(`${label} must be a string.`);
    }

    const trimmed = as.trim();
    const result = validateFieldName(trimmed);
    if (result !== true) {
        throw new Error(`${label} is invalid: ${result}`);
    }

    return trimmed;
}

function normalizeDtoConfig(dto, label) {
    if (dto === undefined || dto === null) {
        return {
            create: true,
            update: true,
            summary: true,
            detail: true,
        };
    }

    if (typeof dto !== 'object' || Array.isArray(dto)) {
        throw new Error(`${label} must be an object.`);
    }

    for (const key of Object.keys(dto)) {
        if (!DTO_KEYS.includes(key)) {
            throw new Error(`${label}.${key} is not allowed. Allowed keys: ${DTO_KEYS.join(', ')}`);
        }
    }

    return {
        create: dto.create !== undefined ? Boolean(dto.create) : true,
        update: dto.update !== undefined ? Boolean(dto.update) : true,
        summary: dto.summary !== undefined ? Boolean(dto.summary) : true,
        detail: dto.detail !== undefined ? Boolean(dto.detail) : true,
    };
}

function normalizeValidationConfig(rawValidation, field, label) {
    if (rawValidation === undefined || rawValidation === null) {
        return {};
    }

    if (typeof rawValidation !== 'object' || Array.isArray(rawValidation)) {
        throw new Error(`${label} must be an object.`);
    }

    const validation = { ...rawValidation };
    const allowedKeys = getAllowedValidationKeys(field.type);

    for (const key of Object.keys(validation)) {
        if (!allowedKeys.has(key)) {
            throw new Error(`${label}.${key} is not allowed for type ${field.type}. Allowed keys: ${Array.from(allowedKeys).join(', ')}`);
        }
    }

    if (STRING_TYPES.has(field.type)) {
        normalizeStringValidation(validation, field, label);
    } else if (INTEGER_TYPES.has(field.type)) {
        normalizeIntegerValidation(validation, label);
    } else if (DECIMAL_TYPES.has(field.type)) {
        normalizeDecimalValidation(validation, label);
    } else if (DATE_TYPES.has(field.type)) {
        normalizeDateValidation(validation, label);
    } else if (field.type === 'Boolean') {
        normalizeBooleanValidation(validation, label);
    } else if (field.type === 'Enum' || field.type === 'ManyToOne') {
        if (Object.keys(validation).length > 0) {
            throw new Error(`${label} is not supported for type ${field.type}.`);
        }
    }

    return validation;
}

function getAllowedValidationKeys(type) {
    if (STRING_TYPES.has(type)) {
        return new Set(['email', 'minLength', 'maxLength', 'pattern', 'patternMessage']);
    }

    if (INTEGER_TYPES.has(type)) {
        return new Set(['min', 'max', 'positive', 'positiveOrZero', 'negative', 'negativeOrZero']);
    }

    if (DECIMAL_TYPES.has(type)) {
        return new Set(['decimalMin', 'decimalMax', 'digits', 'positive', 'positiveOrZero', 'negative', 'negativeOrZero']);
    }

    if (DATE_TYPES.has(type)) {
        return new Set(['past', 'pastOrPresent', 'future', 'futureOrPresent']);
    }

    if (type === 'Boolean') {
        return new Set(['assertTrue', 'assertFalse']);
    }

    return new Set();
}

function normalizeStringValidation(validation, field, label) {
    if (validation.email !== undefined && typeof validation.email !== 'boolean') {
        throw new Error(`${label}.email must be boolean.`);
    }

    if (validation.minLength !== undefined) {
        validation.minLength = parsePositiveInteger(validation.minLength, `${label}.minLength`);
    }

    if (validation.maxLength !== undefined) {
        validation.maxLength = parsePositiveInteger(validation.maxLength, `${label}.maxLength`);
    }

    if (validation.minLength !== undefined && validation.maxLength !== undefined && validation.minLength > validation.maxLength) {
        throw new Error(`${label}.minLength cannot be greater than maxLength.`);
    }

    if (validation.pattern !== undefined && typeof validation.pattern !== 'string') {
        throw new Error(`${label}.pattern must be string.`);
    }

    if (validation.patternMessage !== undefined && typeof validation.patternMessage !== 'string') {
        throw new Error(`${label}.patternMessage must be string.`);
    }

    if (validation.patternMessage && !validation.pattern) {
        throw new Error(`${label}.patternMessage requires pattern.`);
    }

    if (validation.maxLength !== undefined && field.length !== undefined && validation.maxLength > field.length) {
        throw new Error(`${label}.maxLength cannot be greater than field.length (${field.length}).`);
    }
}

function normalizeIntegerValidation(validation, label) {
    if (validation.min !== undefined) {
        validation.min = parseInteger(validation.min, `${label}.min`);
    }

    if (validation.max !== undefined) {
        validation.max = parseInteger(validation.max, `${label}.max`);
    }

    if (validation.min !== undefined && validation.max !== undefined && validation.min > validation.max) {
        throw new Error(`${label}.min cannot be greater than max.`);
    }

    normalizeSignValidation(validation, label);
}

function normalizeDecimalValidation(validation, label) {
    if (validation.decimalMin !== undefined) {
        validation.decimalMin = parseDecimalString(validation.decimalMin, `${label}.decimalMin`);
    }

    if (validation.decimalMax !== undefined) {
        validation.decimalMax = parseDecimalString(validation.decimalMax, `${label}.decimalMax`);
    }

    if (validation.decimalMin !== undefined && validation.decimalMax !== undefined) {
        const min = Number(validation.decimalMin);
        const max = Number(validation.decimalMax);

        if (min > max) {
            throw new Error(`${label}.decimalMin cannot be greater than decimalMax.`);
        }
    }

    if (validation.digits !== undefined) {
        if (!validation.digits || typeof validation.digits !== 'object' || Array.isArray(validation.digits)) {
            throw new Error(`${label}.digits must be an object.`);
        }

        validation.digits.integer = parsePositiveInteger(validation.digits.integer, `${label}.digits.integer`);
        validation.digits.fraction = parseNonNegativeInteger(validation.digits.fraction, `${label}.digits.fraction`);
    }

    normalizeSignValidation(validation, label);
}

function normalizeDateValidation(validation, label) {
    const keys = ['past', 'pastOrPresent', 'future', 'futureOrPresent'];
    const active = [];

    for (const key of keys) {
        if (validation[key] !== undefined && typeof validation[key] !== 'boolean') {
            throw new Error(`${label}.${key} must be boolean.`);
        }

        if (validation[key]) {
            active.push(key);
        }
    }

    if (active.length > 1) {
        throw new Error(`${label} date validation must use only one of: ${keys.join(', ')}.`);
    }
}

function normalizeBooleanValidation(validation, label) {
    if (validation.assertTrue !== undefined && typeof validation.assertTrue !== 'boolean') {
        throw new Error(`${label}.assertTrue must be boolean.`);
    }

    if (validation.assertFalse !== undefined && typeof validation.assertFalse !== 'boolean') {
        throw new Error(`${label}.assertFalse must be boolean.`);
    }

    if (validation.assertTrue && validation.assertFalse) {
        throw new Error(`${label}.assertTrue and assertFalse cannot both be true.`);
    }
}

function normalizeSignValidation(validation, label) {
    const keys = ['positive', 'positiveOrZero', 'negative', 'negativeOrZero'];

    for (const key of keys) {
        if (validation[key] !== undefined && typeof validation[key] !== 'boolean') {
            throw new Error(`${label}.${key} must be boolean.`);
        }
    }

    const active = keys.filter((key) => validation[key]);

    if (active.length > 1) {
        throw new Error(`${label} can only use one of: ${keys.join(', ')}.`);
    }
}

function parseInteger(value, label) {
    const n = Number(value);

    if (!Number.isInteger(n)) {
        throw new Error(`${label} must be an integer.`);
    }

    return n;
}

function parsePositiveInteger(value, label) {
    const n = parseInteger(value, label);

    if (n <= 0) {
        throw new Error(`${label} must be greater than 0.`);
    }

    return n;
}

function parseNonNegativeInteger(value, label) {
    const n = parseInteger(value, label);

    if (n < 0) {
        throw new Error(`${label} must be greater than or equal to 0.`);
    }

    return n;
}

function parseDecimalString(value, label) {
    const s = String(value).trim();

    if (!/^-?\d+(\.\d+)?$/.test(s)) {
        throw new Error(`${label} must be a decimal string.`);
    }

    return s;
}

function assertUniqueEntityNames(resources, sourceLabel) {
    const names = new Set();

    for (const resource of resources) {
        if (names.has(resource.entityName)) {
            throw new Error(`${sourceLabel} has duplicate entityName: ${resource.entityName}`);
        }

        names.add(resource.entityName);
    }
}

function assertUniqueTableNames(resources, pluginPrefix) {
    const tableNames = new Map();

    for (const resource of resources) {
        const tableName = buildTableName(pluginPrefix, resource.entityName);

        if (tableNames.has(tableName)) {
            const existingEntity = tableNames.get(tableName);
            throw new Error(`Duplicate table name "${tableName}" generated from "${existingEntity}" and "${resource.entityName}".`);
        }

        tableNames.set(tableName, resource.entityName);
    }
}

function fieldsWithParent(resource) {
    if (!resource.parent) {
        return resource.fields;
    }

    const parentFieldName = _.lowerFirst(resource.parent);
    if (resource.fields.some((field) => field.name === parentFieldName)) {
        return resource.fields;
    }

    return [
        {
            name: parentFieldName,
            type: 'ManyToOne',
            refEntity: resource.parent,
            required: true,
            unique: false,
            filterable: true,
            dto: {
                create: true,
                update: true,
                summary: true,
                detail: true,
            },
            validation: {},
        },
        ...resource.fields,
    ];
}

async function wireChildResourcesToParentDtos({ pluginDir, packageName, resources }) {
    const generatedOrChanged = [];
    const children = resources.filter((resource) => resource.parent);

    for (const child of children) {
        const parent = resources.find((resource) => resource.entityName === child.parent);
        const parentEntityName = child.parent;
        const childFieldName = child.as || pluralize(_.lowerFirst(child.entityName));
        const shape = inferChildDtoShape(child);

        if (shape === 'objectList') {
            const requestFile = await ensureChildRequestDto({ pluginDir, packageName, child });
            const responseFile = await ensureChildResponseDto({ pluginDir, packageName, child });
            generatedOrChanged.push(requestFile, responseFile);
        }

        const createFile = path.join(pluginDir, 'src', 'main', 'java', ...packageName.split('.'), 'application', 'dto', `${parentEntityName}CreateRequest.java`);
        const updateFile = path.join(pluginDir, 'src', 'main', 'java', ...packageName.split('.'), 'application', 'dto', `${parentEntityName}UpdateRequest.java`);
        const detailFile = path.join(pluginDir, 'src', 'main', 'java', ...packageName.split('.'), 'application', 'dto', `${parentEntityName}DetailResponse.java`);

        const parentExistsInThisRun = Boolean(parent);
        await patchParentDtoFile({
            file: createFile,
            child,
            childFieldName,
            shape,
            dtoKind: 'create',
            required: parentExistsInThisRun,
        });
        await patchParentDtoFile({
            file: updateFile,
            child,
            childFieldName,
            shape,
            dtoKind: 'update',
            required: parentExistsInThisRun,
        });
        await patchParentDtoFile({
            file: detailFile,
            child,
            childFieldName,
            shape,
            dtoKind: 'detail',
            required: parentExistsInThisRun,
        });

        if (shape === 'objectList') {
            const mapperFile = path.join(pluginDir, 'src', 'main', 'java', ...packageName.split('.'), 'application', 'mapper', `${parentEntityName}DtoMapper.java`);
            await patchParentDtoMapperFile({ file: mapperFile, packageName, child, required: parentExistsInThisRun });
            generatedOrChanged.push(mapperFile);
        }

        generatedOrChanged.push(createFile, updateFile, detailFile);
    }

    return [...new Set(generatedOrChanged)];
}

function inferChildDtoShape(child) {
    return child.fields.length === 1 && child.fields[0].type === 'ManyToOne'
        ? 'idSet'
        : 'objectList';
}

async function ensureChildRequestDto({ pluginDir, packageName, child }) {
    const file = dtoFile(pluginDir, packageName, `${child.entityName}Request.java`);
    if (await fs.pathExists(file)) {
        return file;
    }

    await fs.ensureDir(path.dirname(file));
    await writeJavaFile(file, renderChildDto({
        packageName,
        className: `${child.entityName}Request`,
        fields: child.fields,
        response: false,
    }));
    return file;
}

async function ensureChildResponseDto({ pluginDir, packageName, child }) {
    const file = dtoFile(pluginDir, packageName, `${child.entityName}Response.java`);
    if (await fs.pathExists(file)) {
        return file;
    }

    await fs.ensureDir(path.dirname(file));
    await writeJavaFile(file, renderChildDto({
        packageName,
        className: `${child.entityName}Response`,
        fields: child.fields,
        response: true,
    }));
    return file;
}

function dtoFile(pluginDir, packageName, fileName) {
    return path.join(pluginDir, 'src', 'main', 'java', ...packageName.split('.'), 'application', 'dto', fileName);
}

function renderChildDto({ packageName, className, fields, response }) {
    const imports = new Set([
        'lombok.AllArgsConstructor',
        'lombok.Builder',
        'lombok.Data',
        'lombok.NoArgsConstructor',
    ]);

    if (!response) {
        for (const field of fields) {
            if (field.required) {
                imports.add(field.type === 'ManyToOne' || STRING_TYPES.has(field.type)
                    ? 'jakarta.validation.constraints.NotBlank'
                    : 'jakarta.validation.constraints.NotNull');
            }
        }
    }

    collectFieldTypeImports(fields).forEach((importName) => imports.add(importName));

    const fieldLines = fields.map((field) => renderChildDtoField(field, response)).join('\n\n');

    return `package ${packageName}.application.dto;\n\n${[...imports].sort().map((importName) => `import ${importName};`).join('\n')}\n\n@Data\n@Builder\n@NoArgsConstructor\n@AllArgsConstructor\npublic class ${className} {\n\n${fieldLines}\n}\n`;
}

function renderChildDtoField(field, response) {
    const annotations = [];
    const fieldName = field.type === 'ManyToOne' ? `${field.name}Id` : field.name;
    const type = field.type === 'ManyToOne' ? 'String' : childDtoJavaType(field);

    if (!response && field.required) {
        annotations.push(field.type === 'ManyToOne' || STRING_TYPES.has(field.type)
            ? '    @NotBlank'
            : '    @NotNull');
    }

    return `${annotations.length ? annotations.join('\n') + '\n' : ''}    private ${type} ${fieldName};`;
}

function childDtoJavaType(field) {
    if (field.type === 'Enum') return field.enumName;
    if (field.type === 'BigDecimal') return 'BigDecimal';
    if (field.type === 'Date') return 'LocalDate';
    if (field.type === 'DateTime') return 'LocalDateTime';
    if (field.type === 'Instant') return 'Instant';
    if (field.type === 'Integer') return 'Integer';
    if (field.type === 'Long') return 'Long';
    if (field.type === 'Double') return 'Double';
    if (field.type === 'Boolean') return 'Boolean';
    return 'String';
}

function collectFieldTypeImports(fields) {
    const imports = new Set();
    for (const field of fields) {
        if (field.type === 'BigDecimal') imports.add('java.math.BigDecimal');
        if (field.type === 'Date') imports.add('java.time.LocalDate');
        if (field.type === 'DateTime') imports.add('java.time.LocalDateTime');
        if (field.type === 'Instant') imports.add('java.time.Instant');
    }
    return imports;
}

async function patchParentDtoFile({ file, child, childFieldName, shape, dtoKind, required }) {
    if (!(await fs.pathExists(file))) {
        if (required) {
            throw new Error(`Parent DTO file not found for child ${child.entityName}: ${file}`);
        }
        return;
    }

    let source = await fs.readFile(file, 'utf8');
    const original = source;

    if (shape === 'idSet') {
        source = ensureImport(source, 'java.util.Set');
        source = ensureClassField(source, `    private Set<String> ${childFieldName};`);
    } else {
        source = ensureImport(source, 'java.util.List');
        const type = dtoKind === 'detail'
            ? `${child.entityName}Response`
            : `${child.entityName}Request`;

        if (dtoKind !== 'detail') {
            source = ensureImport(source, 'jakarta.validation.Valid');
            source = ensureClassField(source, `    @Valid\n    private List<${type}> ${childFieldName};`);
        } else {
            source = ensureClassField(source, `    private List<${type}> ${childFieldName};`);
        }
    }

    if (source !== original) {
        await writeJavaFile(file, source);
    }
}

function ensureImport(source, importName) {
    const importLine = `import ${importName};`;
    if (source.includes(importLine)) {
        return source;
    }
    return source.replace(/(package [^;]+;\n)/, `$1\n${importLine}\n`);
}

function ensureClassField(source, fieldBlock) {
    const fieldNameMatch = fieldBlock.match(/private [^;]+ ([a-zA-Z0-9_]+);/);
    if (fieldNameMatch && source.includes(` ${fieldNameMatch[1]};`)) {
        return source;
    }

    const insert = `\n${fieldBlock}\n`;
    const index = source.lastIndexOf('\n}');
    if (index === -1) {
        return `${source.trimEnd()}${insert}}\n`;
    }
    return source.slice(0, index) + insert + source.slice(index);
}

async function writeJavaFile(file, source) {
    await fs.writeFile(file, normalizeRenderedContent(source, file), 'utf8');
}

async function patchParentDtoMapperFile({ file, packageName, child, required }) {
    if (!(await fs.pathExists(file))) {
        if (required) {
            throw new Error(`Parent DTO mapper file not found for child ${child.entityName}: ${file}`);
        }
        return;
    }

    let source = await fs.readFile(file, 'utf8');
    const original = source;

    source = ensureImport(source, `${packageName}.application.dto.${child.entityName}Response`);
    source = ensureImport(source, `${packageName}.domain.model.${child.entityName}`);
    source = ensureImport(source, 'org.springframework.beans.factory.annotation.Autowired');
    source = ensureAbstractMapper(source);
    source = ensureMapperIdEncoderField(source);
    source = ensureChildResponseMapperMethod(source, child);

    if (source !== original) {
        await writeJavaFile(file, source);
    }
}

function ensureAbstractMapper(source) {
    return source.replace(/public interface ([A-Za-z0-9_]+DtoMapper) extends /, 'public abstract class $1 implements ');
}

function ensureMapperIdEncoderField(source) {
    if (source.includes('protected IdEncoder idEncoder;')) {
        return source;
    }
    source = ensureImport(source, 'org.springframework.beans.factory.annotation.Autowired');
    return source.replace(/(\{\n)/, '$1\n    @Autowired\n    protected IdEncoder idEncoder;\n');
}

function ensureChildResponseMapperMethod(source, child) {
    const methodName = `to${child.entityName}Response`;
    if (source.includes(`${methodName}(`)) {
        return source;
    }

    const method = renderChildResponseMapperMethod(child);
    const index = source.lastIndexOf('\n}');
    if (index === -1) {
        return `${source.trimEnd()}\n${method}\n}\n`;
    }
    return source.slice(0, index) + `\n${method}\n` + source.slice(index);
}

function renderChildResponseMapperMethod(child) {
    const responseFields = child.fields.map((field) => {
        if (field.type === 'ManyToOne') {
            return `                .${field.name}Id(${_.lowerFirst(child.entityName)}.get${_.upperFirst(field.name)}() != null
                        ? idEncoder.encode(${_.lowerFirst(child.entityName)}.get${_.upperFirst(field.name)}().getId())
                        : null)`;
        }
        return `                .${field.name}(${_.lowerFirst(child.entityName)}.get${_.upperFirst(field.name)}())`;
    }).join('\n');

    return `    public ${child.entityName}Response to${child.entityName}Response(${child.entityName} ${_.lowerFirst(child.entityName)}) {
        if (${_.lowerFirst(child.entityName)} == null) {
            return null;
        }
        return ${child.entityName}Response.builder()
${responseFields}
                .build();
    }
`;
}

async function wireChildResourcesToParentServices({ pluginDir, packageName, resources }) {
    const changed = [];
    const childrenByParent = new Map();

    for (const child of resources.filter((resource) => resource.parent)) {
        if (!childrenByParent.has(child.parent)) {
            childrenByParent.set(child.parent, []);
        }
        childrenByParent.get(child.parent).push(child);
    }

    for (const [parentEntityName, children] of childrenByParent.entries()) {
        const file = path.join(pluginDir, 'src', 'main', 'java', ...packageName.split('.'), 'application', 'service', `${parentEntityName}ServiceImpl.java`);
        if (!(await fs.pathExists(file))) {
            continue;
        }

        const existing = await fs.readFile(file, 'utf8');
        if (!isBasicGeneratedService(existing, parentEntityName)) {
            continue;
        }

        await writeJavaFile(file, renderParentService({ packageName, parentEntityName, children }));
        changed.push(file);
    }

    return changed;
}

function isBasicGeneratedService(source, entityName) {
    return source.includes(`class ${entityName}ServiceImpl`)
        && !source.includes(`public ${entityName}DetailResponse create(`)
        && !source.includes(`private ${entityName}DetailResponse enrichDetail(`);
}

function renderParentService({ packageName, parentEntityName, children }) {
    const parentVar = _.lowerFirst(parentEntityName);
    const childShapes = children.map((child) => ({
        child,
        shape: inferChildDtoShape(child),
        fieldName: child.as || pluralize(_.lowerFirst(child.entityName)),
        parentFieldName: _.lowerFirst(parentEntityName),
    }));
    const targetFields = uniqueTargetFields(children);
    const imports = renderParentServiceImports({ packageName, parentEntityName, childShapes, targetFields });
    const fields = renderParentServiceFields({ parentEntityName, childShapes, targetFields });
    const constructor = renderParentServiceConstructor({ parentEntityName, parentVar, childShapes, targetFields });
    const createArgs = childShapes.map(({ fieldName }) => `request.get${_.upperFirst(fieldName)}()`).join(', ');
    const resolvedArgs = createArgs ? createArgs : '';

    return `package ${packageName}.application.service;\n\n${imports}\n@Service\npublic class ${parentEntityName}ServiceImpl\n        extends BaseServiceImpl<${parentEntityName}, ${parentEntityName}CreateRequest, ${parentEntityName}UpdateRequest, ${parentEntityName}SummaryResponse, ${parentEntityName}DetailResponse>\n        implements ${parentEntityName}Service {\n\n${fields}\n${constructor}\n    @Override\n    protected String resourceType() {\n        return \"${parentEntityName}\";\n    }\n\n    @Override\n    @Transactional\n    public ${parentEntityName}DetailResponse create(${parentEntityName}CreateRequest request) {\n        ResolvedRefs refs = validateAndResolveIds(${resolvedArgs});\n        ${parentEntityName}DetailResponse response = super.create(request);\n        Long ${parentVar}Id = getIdFromResponse(response);\n        ${parentEntityName} ${parentVar} = ${parentVar}RepositoryPort.findById(${parentVar}Id).orElse(null);\n\n${childShapes.map(({ child, fieldName }) => `        save${child.entityName}s(request.get${_.upperFirst(fieldName)}(), ${parentVar}, refs);`).join('\n')}\n\n        return enrichDetail(response, ${parentVar}Id);\n    }\n\n    @Override\n    @Transactional\n    public ${parentEntityName}DetailResponse update(Long id, ${parentEntityName}UpdateRequest request) {\n        ResolvedRefs refs = validateAndResolveIds(${resolvedArgs});\n        ${parentEntityName}DetailResponse response = super.update(id, request);\n        ${parentEntityName} ${parentVar} = ${parentVar}RepositoryPort.findById(id).orElse(null);\n\n${childShapes.map(({ child }) => `        delete${child.entityName}s(id);`).join('\n')}\n${childShapes.map(({ child, fieldName }) => `        save${child.entityName}s(request.get${_.upperFirst(fieldName)}(), ${parentVar}, refs);`).join('\n')}\n\n        return enrichDetail(response, id);\n    }\n\n    @Override\n    public ${parentEntityName}DetailResponse findById(Long id) {\n        ${parentEntityName}DetailResponse response = super.findById(id);\n        return enrichDetail(response, id);\n    }\n\n${renderEnrichDetail({ parentEntityName, childShapes })}\n${childShapes.map(renderChildFindDeleteSaveMethods).join('\n')}\n${renderValidateAndResolve({ childShapes, targetFields })}\n${renderResolvedRefs(targetFields)}\n${renderGenericHelpers({ parentEntityName })}\n}\n`;
}

function renderParentServiceImports({ packageName, parentEntityName, childShapes, targetFields }) {
    const imports = new Set([
        'java.util.Collections',
        'java.util.List',
        'java.util.Map',
        'java.util.Set',
        'java.util.function.Function',
        'java.util.stream.Collectors',
        'org.springframework.data.jpa.domain.Specification',
        'org.springframework.stereotype.Service',
        'org.springframework.transaction.annotation.Transactional',
        `${packageName}.application.dto.${parentEntityName}CreateRequest`,
        `${packageName}.application.dto.${parentEntityName}DetailResponse`,
        `${packageName}.application.dto.${parentEntityName}SummaryResponse`,
        `${packageName}.application.dto.${parentEntityName}UpdateRequest`,
        `${packageName}.application.mapper.${parentEntityName}DtoMapper`,
        `${packageName}.domain.model.${parentEntityName}`,
        `${packageName}.domain.port.inbound.${parentEntityName}Service`,
        `${packageName}.domain.port.outbound.${parentEntityName}RepositoryPort`,
        'gasi.gps.core.api.application.exception.BusinessException',
        'gasi.gps.core.api.domain.model.BaseModel',
        'gasi.gps.core.api.domain.model.SimpleFilter',
        'gasi.gps.core.api.domain.model.SortOrder',
        'gasi.gps.core.api.domain.port.outbound.BaseRepositoryPort',
        'gasi.gps.core.starter.application.service.BaseServiceImpl',
        'gasi.gps.core.starter.infrastructure.i18n.MessageUtil',
        'gasi.gps.core.starter.infrastructure.specification.GenericSpecification',
        'gasi.gps.core.starter.infrastructure.util.IdEncoder',
    ]);

    for (const { child, shape } of childShapes) {
        imports.add(`${packageName}.domain.model.${child.entityName}`);
        imports.add(`${packageName}.infrastructure.entity.${child.entityName}Entity`);
        imports.add(`${packageName}.infrastructure.mapper.${child.entityName}Mapper`);
        imports.add(`${packageName}.infrastructure.persistence.${child.entityName}EntityRepository`);
        if (shape === 'objectList') {
            imports.add(`${packageName}.application.dto.${child.entityName}Request`);
            imports.add(`${packageName}.application.dto.${child.entityName}Response`);
        } else {
            imports.add(`${packageName}.infrastructure.entity.${parentEntityName}Entity`);
        }
        for (const field of child.fields) {
            if (field.type === 'ManyToOne') {
                imports.add(`${packageName}.domain.model.${field.refEntity}`);
                imports.add(`${packageName}.domain.port.outbound.${field.refEntity}RepositoryPort`);
                if (shape === 'idSet') {
                    imports.add(`${packageName}.infrastructure.entity.${field.refEntity}Entity`);
                }
            }
        }
    }

    targetFields.forEach((field) => {
        imports.add(`${packageName}.domain.model.${field.refEntity}`);
        imports.add(`${packageName}.domain.port.outbound.${field.refEntity}RepositoryPort`);
    });

    return [...imports].sort().map((importName) => `import ${importName};`).join('\n') + '\n\n';
}

function renderParentServiceFields({ parentEntityName, childShapes, targetFields }) {
    const parentVar = _.lowerFirst(parentEntityName);
    const lines = [
        `    private final ${parentEntityName}RepositoryPort ${parentVar}RepositoryPort;`,
        `    private final ${parentEntityName}DtoMapper ${parentVar}DtoMapper;`,
    ];
    for (const { child } of childShapes) {
        const childVar = _.lowerFirst(child.entityName);
        lines.push(`    private final ${child.entityName}EntityRepository ${childVar}EntityRepository;`);
        lines.push(`    private final ${child.entityName}Mapper ${childVar}Mapper;`);
    }
    for (const field of targetFields) {
        lines.push(`    private final ${field.refEntity}RepositoryPort ${_.lowerFirst(field.refEntity)}RepositoryPort;`);
    }
    return lines.join('\n') + '\n\n';
}

function renderParentServiceConstructor({ parentEntityName, parentVar, childShapes, targetFields }) {
    const params = [
        `${parentEntityName}RepositoryPort repositoryPort`,
        `${parentEntityName}DtoMapper dtoMapper`,
        'MessageUtil messageUtil',
        'IdEncoder idEncoder',
        ...childShapes.flatMap(({ child }) => [
            `${child.entityName}EntityRepository ${_.lowerFirst(child.entityName)}EntityRepository`,
            `${child.entityName}Mapper ${_.lowerFirst(child.entityName)}Mapper`,
        ]),
        ...targetFields.map((field) => `${field.refEntity}RepositoryPort ${_.lowerFirst(field.refEntity)}RepositoryPort`),
    ];
    const assignments = [
        `        this.${parentVar}RepositoryPort = repositoryPort;`,
        `        this.${parentVar}DtoMapper = dtoMapper;`,
        ...childShapes.flatMap(({ child }) => {
            const childVar = _.lowerFirst(child.entityName);
            return [
                `        this.${childVar}EntityRepository = ${childVar}EntityRepository;`,
                `        this.${childVar}Mapper = ${childVar}Mapper;`,
            ];
        }),
        ...targetFields.map((field) => `        this.${_.lowerFirst(field.refEntity)}RepositoryPort = ${_.lowerFirst(field.refEntity)}RepositoryPort;`),
    ];

    return `    public ${parentEntityName}ServiceImpl(${params.join(',\n            ')}) {\n        super(repositoryPort, dtoMapper, messageUtil, idEncoder);\n${assignments.join('\n')}\n    }\n\n`;
}

function renderEnrichDetail({ parentEntityName, childShapes }) {
    const lines = [
        `    private ${parentEntityName}DetailResponse enrichDetail(${parentEntityName}DetailResponse response, Long ${_.lowerFirst(parentEntityName)}Id) {`,
        ...childShapes.map(({ child, fieldName }) => `        response.set${_.upperFirst(fieldName)}(to${child.entityName}Responses(find${child.entityName}s(${_.lowerFirst(parentEntityName)}Id)));`),
        '        return response;',
        '    }',
    ];
    return lines.join('\n') + '\n\n';
}

function renderChildFindDeleteSaveMethods({ child, shape, fieldName, parentFieldName }) {
    const childVar = _.lowerFirst(child.entityName);
    const parentType = _.upperFirst(parentFieldName);
    if (shape === 'idSet') {
        const target = child.fields[0];
        return `    private List<${child.entityName}Entity> find${child.entityName}s(Long ${parentFieldName}Id) {\n        Specification<${child.entityName}Entity> spec = GenericSpecification.from(parentFilter(${parentFieldName}Id));\n        return ${childVar}EntityRepository.findAll(spec);\n    }\n\n    private void delete${child.entityName}s(Long ${parentFieldName}Id) {\n        List<${child.entityName}Entity> entities = find${child.entityName}s(${parentFieldName}Id);\n        if (!entities.isEmpty()) {\n            ${childVar}EntityRepository.deleteAllInBatch(entities);\n        }\n    }\n\n    private void save${child.entityName}s(Set<String> ${fieldName}, ${parentType} ${parentFieldName}, ResolvedRefs refs) {\n        if (${fieldName} == null || ${fieldName}.isEmpty()) {\n            return;\n        }\n        ${parentType}Entity ${parentFieldName}Entity = ${parentType}Entity.builder().id(${parentFieldName}.getId()).build();\n        List<${child.entityName}Entity> entities = ${fieldName}.stream()\n                .map(idEncoder::decode)\n                .map(${target.name}Id -> ${child.entityName}Entity.builder()\n                        .${parentFieldName}(${parentFieldName}Entity)\n                        .${target.name}(${target.refEntity}Entity.builder().id(${target.name}Id).build())\n                        .build())\n                .collect(Collectors.toList());\n        ${childVar}EntityRepository.saveAll(entities);\n    }\n\n    private Set<String> to${child.entityName}Responses(List<${child.entityName}Entity> entities) {\n        return entities.stream()\n                .map(entity -> idEncoder.encode(entity.get${_.upperFirst(target.name)}().getId()))\n                .collect(Collectors.toSet());\n    }\n`;
    }

    const requestType = `${child.entityName}Request`;
    const responseType = `${child.entityName}Response`;
    const saveSetters = child.fields.map((field) => {
        if (field.type === 'ManyToOne') {
            return `                            .${field.name}(refs.${_.lowerFirst(field.refEntity)}Map.get(idEncoder.decode(req.get${_.upperFirst(field.name)}Id())))`;
        }
        return `                            .${field.name}(req.get${_.upperFirst(field.name)}())`;
    }).join('\n');
    return `    private List<${child.entityName}Entity> find${child.entityName}s(Long ${parentFieldName}Id) {\n        Specification<${child.entityName}Entity> spec = GenericSpecification.from(parentFilter(${parentFieldName}Id));\n        return ${childVar}EntityRepository.findAll(spec);\n    }\n\n    private void delete${child.entityName}s(Long ${parentFieldName}Id) {\n        List<${child.entityName}Entity> entities = find${child.entityName}s(${parentFieldName}Id);\n        if (!entities.isEmpty()) {\n            ${childVar}EntityRepository.deleteAllInBatch(entities);\n        }\n    }\n\n    private void save${child.entityName}s(List<${requestType}> requests, ${parentType} ${parentFieldName}, ResolvedRefs refs) {\n        if (requests == null || requests.isEmpty()) {\n            return;\n        }\n        List<${child.entityName}Entity> entities = requests.stream()\n                .map(req -> {\n                    ${child.entityName} ${childVar} = ${child.entityName}.builder()\n                            .${parentFieldName}(${parentFieldName})\n${saveSetters}\n                            .build();\n                    return ${childVar}Mapper.toEntity(${childVar});\n                })\n                .collect(Collectors.toList());\n        ${childVar}EntityRepository.saveAll(entities);\n    }\n\n    private List<${responseType}> to${child.entityName}Responses(List<${child.entityName}Entity> entities) {\n        return entities.stream()\n                .map(entity -> ${childVar}Mapper.toDomain(entity))\n                .map(${parentFieldName}DtoMapper::to${child.entityName}Response)\n                .collect(Collectors.toList());\n    }\n`;
}

function renderValidateAndResolve({ childShapes, targetFields }) {
    const params = childShapes.map(({ shape, child, fieldName }) => {
        if (shape === 'idSet') return `Set<String> ${fieldName}`;
        return `List<${child.entityName}Request> ${fieldName}`;
    });
    const initMaps = targetFields.map((field) => `        Map<Long, ${field.refEntity}> ${_.lowerFirst(field.refEntity)}Map = Collections.emptyMap();`).join('\n');
    const validationBlocks = childShapes.map(({ child, shape, fieldName }) => renderChildValidationBlock({ child, shape, fieldName })).join('\n\n');
    const returnArgs = targetFields.map((field) => `${_.lowerFirst(field.refEntity)}Map`).join(', ');

    return `    private ResolvedRefs validateAndResolveIds(${params.join(', ')}) {\n        BusinessException.Collector collector = new BusinessException.Collector();\n\n${initMaps}\n\n${validationBlocks}\n\n        collector.validate();\n        return new ResolvedRefs(${returnArgs});\n    }\n\n`;
}

function renderChildValidationBlock({ child, shape, fieldName }) {
    if (shape === 'idSet') {
        const target = child.fields[0];
        const targetVar = _.lowerFirst(target.refEntity);
        return `        if (${fieldName} != null && !${fieldName}.isEmpty()) {\n            Set<Long> ${target.name}Ids = ${fieldName}.stream()\n                    .map(idEncoder::decode)\n                    .collect(Collectors.toSet());\n            ${targetVar}Map = batchFetch(${targetVar}RepositoryPort, ${target.name}Ids);\n            collectMissing(${target.name}Ids, ${targetVar}Map, \"${target.name}Id\", collector);\n        }`;
    }

    const manyToOneFields = child.fields.filter((field) => field.type === 'ManyToOne');
    const idSets = manyToOneFields.map((field) => `            Set<Long> ${field.name}Ids = ${fieldName}.stream()\n                    .map(req -> idEncoder.decode(req.get${_.upperFirst(field.name)}Id()))\n                    .collect(Collectors.toSet());`).join('\n');
    const fetches = manyToOneFields.map((field) => {
        const targetVar = _.lowerFirst(field.refEntity);
        return `            ${targetVar}Map = batchFetch(${targetVar}RepositoryPort, ${field.name}Ids);\n            collectMissing(${field.name}Ids, ${targetVar}Map, \"${field.name}Id\", collector);`;
    }).join('\n');

    return `        if (${fieldName} != null && !${fieldName}.isEmpty()) {\n${idSets}\n\n${fetches}\n        }`;
}

function renderResolvedRefs(targetFields) {
    const fields = targetFields.map((field) => `        final Map<Long, ${field.refEntity}> ${_.lowerFirst(field.refEntity)}Map;`).join('\n');
    const params = targetFields.map((field) => `Map<Long, ${field.refEntity}> ${_.lowerFirst(field.refEntity)}Map`).join(', ');
    const assignments = targetFields.map((field) => `            this.${_.lowerFirst(field.refEntity)}Map = ${_.lowerFirst(field.refEntity)}Map;`).join('\n');

    return `    private static class ResolvedRefs {\n${fields}\n\n        ResolvedRefs(${params}) {\n${assignments}\n        }\n    }\n\n`;
}

function renderGenericHelpers({ parentEntityName }) {
    const parentFieldName = _.lowerFirst(parentEntityName);
    return `    private SimpleFilter parentFilter(Long ${parentFieldName}Id) {\n        return SimpleFilter.builder()\n                .field(\"${parentFieldName}.id\")\n                .operator(SimpleFilter.FilterOperator.EQUALS)\n                .value(${parentFieldName}Id)\n                .build();\n    }\n\n    private <T extends BaseModel> Map<Long, T> batchFetch(BaseRepositoryPort<T> port, Set<Long> ids) {\n        if (ids.isEmpty()) {\n            return Collections.emptyMap();\n        }\n        SimpleFilter inFilter = SimpleFilter.builder()\n                .field(\"id\")\n                .operator(SimpleFilter.FilterOperator.IN)\n                .value(ids)\n                .build();\n        List<T> results = port.findAll(inFilter, Collections.<SortOrder>emptyList());\n        return results.stream()\n                .collect(Collectors.toMap(BaseModel::getId, Function.identity()));\n    }\n\n    private <T> void collectMissing(Set<Long> requestedIds, Map<Long, T> foundMap,\n            String fieldName, BusinessException.Collector collector) {\n        requestedIds.stream()\n                .filter(id -> !foundMap.containsKey(id))\n                .map(idEncoder::encode)\n                .forEach(encodedId -> collector.add(\"Invalid \" + fieldName + \": \" + encodedId));\n    }\n\n    private Long getIdFromResponse(${parentEntityName}DetailResponse response) {\n        return idEncoder.decode(response.getId());\n    }\n`;
}

function uniqueTargetFields(children) {
    const fields = new Map();
    for (const child of children) {
        for (const field of child.fields) {
            if (field.type === 'ManyToOne') {
                fields.set(field.refEntity, field);
            }
        }
    }
    return [...fields.values()];
}

function buildTableName(pluginPrefix, entityName) {
    const baseTableName = pluralize(_.snakeCase(entityName));

    if (!pluginPrefix) {
        return baseTableName;
    }

    return `${pluginPrefix}_${baseTableName}`;
}

async function loadResourceSpecsFromFiles(filePaths, cwd, fallbackEntityName) {
    const resources = [];

    for (const filePath of filePaths) {
        const loaded = await loadResourceSpecFile(filePath, cwd);

        const sourceLabel = path.relative(cwd, loaded.file);
        const normalized = normalizeResourceSpecDocument(
            loaded.spec,
            filePaths.length === 1 ? fallbackEntityName : null,
            sourceLabel,
        );

        for (const resource of normalized) {
            resources.push({
                ...resource,
                sourceFile: loaded.file,
            });
        }
    }

    assertUniqueEntityNames(resources, 'resource files');

    return resources;
}

async function promptFields() {
    const fields = [];
    console.log(chalk.gray('\n  ── Add Fields ──\n'));

    let addMore = true;
    while (addMore) {
        const field = await promptField(fields);
        fields.push(field);

        const { cont } = await inquirer.prompt([
            { type: 'confirm', name: 'cont', message: 'Add another field?', default: true },
        ]);

        addMore = cont;
    }

    return fields;
}

function normalizeFileOptions(fileOption) {
    if (!fileOption) {
        return [];
    }

    if (Array.isArray(fileOption)) {
        return fileOption.filter(Boolean);
    }

    return [fileOption];
}

async function resourceCreate(entityName, opts) {
    console.log(chalk.cyan.bold('\n  gasi — Resource Generator\n'));

    const filePaths = normalizeFileOptions(opts.file);

    if (entityName) {
        const result = validateEntityName(entityName);
        if (result !== true) throw new Error(result);
    }

    // ── 1. Detect or select target plugin ──────────────────────────────
    let cwd, pluginModule, pluginDir;

    const detected = await detectPluginFromCwd(resolveCwd(opts));
    if (detected) {
        cwd = detected.projectRoot;
        pluginModule = detected.pluginModule;
        pluginDir = detected.pluginDir;
        console.log(`  Plugin: ${chalk.green(pluginModule)}\n`);
    } else {
        cwd = resolveCwd(opts);
        await assertProjectRoot(cwd);

        const pluginModules = await getPluginModules(cwd);
        if (!pluginModules.length) {
            throw new Error('No plugin modules found in the parent pom.xml. Create a plugin first with "gasi plugin create".');
        }

        const pluginChoices = [];
        for (const mod of pluginModules) {
            const modDir = path.join(cwd, mod);
            const metadata = await resolvePluginMetadata(modDir);

            if (metadata && metadata.packageName) {
                pluginChoices.push(mod);
            }
        }

        if (!pluginChoices.length) {
            throw new Error('No valid plugin modules found. Ensure plugin.properties exists in each plugin resources directory.');
        }

        const answer = await inquirer.prompt([
            {
                type: 'list',
                name: 'pluginModule',
                message: 'Target plugin:',
                choices: pluginChoices,
            },
        ]);

        pluginModule = answer.pluginModule;
        pluginDir = path.join(cwd, pluginModule);
    }

    // Derive plugin metadata from module name (e.g. "plugins/payroll-plugin")
    const pluginName = path.basename(pluginModule).replace(/-plugin$/, '');

    const pluginMetadata = await resolvePluginMetadata(pluginDir);

    if (!pluginMetadata) {
        const propsFile = path.join(pluginDir, 'src', 'main', 'resources', 'plugin.properties');
        throw new Error(`plugin.properties is required in ${propsFile}`);
    }

    const pluginPrefixRaw = pluginMetadata.pluginPrefix
        ? pluginMetadata.pluginPrefix.trim()
        : '';

    const pluginPrefix = pluginPrefixRaw
        ? _.snakeCase(pluginPrefixRaw).toLowerCase()
        : '';

    // ── 2. Collect resource specs ──────────────────────────────────────
    let resourceSpecs;

    if (filePaths.length > 0) {
        resourceSpecs = await loadResourceSpecsFromFiles(filePaths, cwd, entityName);

        console.log(chalk.gray(`  Loaded resource definition file(s): ${filePaths.length}`));
        console.log(chalk.gray(`  Resources: ${resourceSpecs.length}\n`));
    } else {
        if (!entityName) {
            throw new Error('Entity name is required when --file is not provided.');
        }

        const fields = await promptFields();

        resourceSpecs = [
            {
                entityName,
                fields,
                sourceFile: null,
            },
        ];
    }

    assertUniqueTableNames(resourceSpecs, pluginPrefix);

    // ── 3. Summary & confirm ───────────────────────────────────────────
    console.log('\n' + chalk.bold('Summary:'));
    console.log(`  Plugin         : ${chalk.green(pluginModule)}`);
    console.log(`  Prefix         : ${pluginPrefixRaw ? chalk.green(pluginPrefixRaw) : chalk.gray('(none)')}`);
    console.log(`  Resources      : ${chalk.green(resourceSpecs.length)}`);

    for (const resource of resourceSpecs) {
        const tableName = buildTableName(pluginPrefix, resource.entityName);

        console.log();
        console.log(`  Entity         : ${chalk.green(resource.entityName)}`);
        console.log(`  Table          : ${chalk.green(tableName)}`);
        if (resource.parent) {
            console.log(`  Parent         : ${chalk.green(resource.parent)}`);
            console.log(`  Parent field   : ${chalk.green(_.lowerFirst(resource.parent))}`);
            console.log(`  Parent DTO as  : ${resource.as ? chalk.green(resource.as) : chalk.gray('(default)')}`);
            console.log(`  Expose API     : ${resource.exposeApi ? chalk.green('yes') : chalk.gray('no')}`);
        }

        if (resource.sourceFile) {
            console.log(`  Source file    : ${chalk.gray(path.relative(cwd, resource.sourceFile))}`);
        }

        console.log(`  Fields         :`);

        for (const f of resource.fields) {
            const typeLabel = f.type === 'ManyToOne'
                ? `→${f.refEntity}`
                : f.type === 'Enum'
                    ? `Enum(${f.enumName})`
                    : f.type;

            const flags = [];
            if (f.required) flags.push('required');
            if (f.unique) flags.push('unique');
            if (f.filterable) flags.push('filterable');

            const flagStr = flags.length ? chalk.gray(` [${flags.join(', ')}]`) : '';
            const dtoStr = chalk.gray(formatDtoFlags(f.dto));
            const validationStr = chalk.gray(formatValidationFlags(f.validation));
            console.log(`    - ${chalk.cyan(f.name)} ${chalk.yellow(typeLabel)}${flagStr}${dtoStr}${validationStr}`);
        }
    }

    console.log();

    if (!opts.yes) {
        const { confirm } = await inquirer.prompt([
            { type: 'confirm', name: 'confirm', message: 'Generate resource?', default: true },
        ]);

        if (!confirm) {
            console.log(chalk.yellow('Cancelled.'));
            return;
        }
    }

    // ── 4. Generate ────────────────────────────────────────────────────
    const spinner = ora('Generating resource files...').start();

    try {
        const allGeneratedFiles = [];

        for (const resource of resourceSpecs) {
            const tableName = buildTableName(pluginPrefix, resource.entityName);
            const fields = fieldsWithParent(resource);

            const generatedFiles = await generateResource({
                cwd,
                pluginDir,
                pluginName,
                pluginPrefix,
                pluginPrefixRaw,
                entityName: resource.entityName,
                tableName,
                fields,
                parent: resource.parent,
                as: resource.as,
                exposeApi: resource.exposeApi,
            });

            allGeneratedFiles.push(...generatedFiles);
        }

        const childDtoFiles = await wireChildResourcesToParentDtos({
            pluginDir,
            packageName: pluginMetadata.packageName,
            resources: resourceSpecs,
        });
        const parentServiceFiles = await wireChildResourcesToParentServices({
            pluginDir,
            packageName: pluginMetadata.packageName,
            resources: resourceSpecs,
        });

        allGeneratedFiles.push(...childDtoFiles);
        allGeneratedFiles.push(...parentServiceFiles);

        spinner.succeed(`Generated ${allGeneratedFiles.length} files from ${resourceSpecs.length} resource(s).`);
        console.log();

        for (const f of allGeneratedFiles) {
            const rel = path.relative(cwd, f);
            console.log(chalk.gray('    ' + rel));
        }
    } catch (err) {
        spinner.fail('Failed to generate resource.');
        throw err;
    }

    console.log(chalk.green.bold('\n✓ Resource created successfully!\n'));
    console.log(chalk.bold('Next steps:'));
    console.log(chalk.gray('  1.') + ' Review and adjust the generated files (DTOs, validation, etc.)');
    console.log(chalk.gray('  2.') + ` Build: ${chalk.cyan('gasi plugin build ' + pluginName)}`);
    console.log(chalk.gray('  3.') + ` Deploy: ${chalk.cyan('gasi plugin deploy ' + pluginName)}\n`);
}

/**
 * Prompt the user for a single field definition.
 */
async function promptField(existingFields) {
    const existingNames = new Set(existingFields.map((f) => f.name));

    const { name } = await inquirer.prompt([
        {
            type: 'input',
            name: 'name',
            message: 'Field name (camelCase):',
            validate: (v) => {
                const result = validateFieldName(v);
                if (result !== true) return result;
                if (existingNames.has(v.trim())) return `Field "${v.trim()}" already exists.`;
                return true;
            },
            filter: (v) => v.trim(),
        },
    ]);

    const { type } = await inquirer.prompt([
        {
            type: 'list',
            name: 'type',
            message: `Type for "${name}":`,
            choices: FIELD_TYPES,
        },
    ]);

    const field = { name, type };

    // Type-specific follow-ups
    if (type === 'String') {
        const { length } = await inquirer.prompt([
            {
                type: 'input',
                name: 'length',
                message: 'Max length:',
                default: '255',
                validate: (v) => {
                    const n = parseInt(v, 10);
                    return n > 0 ? true : 'Must be a positive number.';
                },
                filter: (v) => parseInt(v, 10),
            },
        ]);

        field.length = length;
    }

    if (type === 'ManyToOne') {
        const { refEntity } = await inquirer.prompt([
            {
                type: 'input',
                name: 'refEntity',
                message: 'Reference entity (PascalCase, e.g. Department):',
                validate: validateEntityName,
                filter: (v) => v.trim(),
            },
        ]);

        field.refEntity = refEntity;
    }

    if (type === 'Enum') {
        const { enumName } = await inquirer.prompt([
            {
                type: 'input',
                name: 'enumName',
                message: 'Enum class name (PascalCase, e.g. EmployeeStatus):',
                validate: validateEnumName,
                filter: (v) => v.trim(),
            },
        ]);

        field.enumName = enumName;
    }

    // Common flags
    const commonQuestions = [
        { type: 'confirm', name: 'required', message: `Required?`, default: true },
        { type: 'confirm', name: 'filterable', message: `Filterable?`, default: false },
    ];

    // Unique only makes sense for certain types
    if (['String', 'Integer', 'Long'].includes(type)) {
        commonQuestions.push(
            { type: 'confirm', name: 'unique', message: `Unique?`, default: false },
        );
    }

    const flags = await inquirer.prompt(commonQuestions);

    field.required = flags.required;
    field.unique = flags.unique || false;
    field.filterable = flags.filterable || false;

    const { dtoIncludes } = await inquirer.prompt([
        {
            type: 'checkbox',
            name: 'dtoIncludes',
            message: `Include "${name}" in DTOs:`,
            choices: [
                { name: 'Create Request', value: 'create', checked: true },
                { name: 'Update Request', value: 'update', checked: true },
                { name: 'Summary Response', value: 'summary', checked: true },
                { name: 'Detail Response', value: 'detail', checked: true },
            ],
        },
    ]);

    field.dto = {
        create: dtoIncludes.includes('create'),
        update: dtoIncludes.includes('update'),
        summary: dtoIncludes.includes('summary'),
        detail: dtoIncludes.includes('detail'),
    };

    field.validation = await promptExtraValidation(field);

    return field;
}

async function promptExtraValidation(field) {
    if (field.type === 'Enum' || field.type === 'ManyToOne') {
        return {};
    }

    const choices = getInteractiveValidationChoices(field.type);

    if (!choices.length) {
        return {};
    }

    const { addValidation } = await inquirer.prompt([
        {
            type: 'confirm',
            name: 'addValidation',
            message: `Add extra validation for "${field.name}"?`,
            default: false,
        },
    ]);

    if (!addValidation) {
        return {};
    }

    if (DATE_TYPES.has(field.type)) {
        return promptDateValidation(field);
    }

    if (field.type === 'Boolean') {
        return promptBooleanValidation(field);
    }

    const { selected } = await inquirer.prompt([
        {
            type: 'checkbox',
            name: 'selected',
            message: `Extra validations for "${field.name}":`,
            choices,
            validate: (values) => validateInteractiveValidationSelection(values, field.type),
        },
    ]);

    const validation = {};

    if (STRING_TYPES.has(field.type)) {
        await fillStringValidation(validation, selected, field);
    } else if (INTEGER_TYPES.has(field.type)) {
        await fillIntegerValidation(validation, selected, field);
    } else if (DECIMAL_TYPES.has(field.type)) {
        await fillDecimalValidation(validation, selected, field);
    }

    return normalizeValidationConfig(validation, field, `${field.name}.validation`);
}

function getInteractiveValidationChoices(type) {
    if (STRING_TYPES.has(type)) {
        return [
            { name: 'Email', value: 'email' },
            { name: 'Min Length', value: 'minLength' },
            { name: 'Max Length', value: 'maxLength' },
            { name: 'Pattern / Regex', value: 'pattern' },
        ];
    }

    if (INTEGER_TYPES.has(type)) {
        return [
            { name: 'Min', value: 'min' },
            { name: 'Max', value: 'max' },
            { name: 'Positive', value: 'positive' },
            { name: 'Positive Or Zero', value: 'positiveOrZero' },
            { name: 'Negative', value: 'negative' },
            { name: 'Negative Or Zero', value: 'negativeOrZero' },
        ];
    }

    if (DECIMAL_TYPES.has(type)) {
        return [
            { name: 'Decimal Min', value: 'decimalMin' },
            { name: 'Decimal Max', value: 'decimalMax' },
            { name: 'Digits', value: 'digits' },
            { name: 'Positive', value: 'positive' },
            { name: 'Positive Or Zero', value: 'positiveOrZero' },
            { name: 'Negative', value: 'negative' },
            { name: 'Negative Or Zero', value: 'negativeOrZero' },
        ];
    }

    return [];
}

function validateInteractiveValidationSelection(values, type) {
    if (!values || values.length === 0) {
        return true;
    }

    if (INTEGER_TYPES.has(type) || DECIMAL_TYPES.has(type)) {
        const signKeys = ['positive', 'positiveOrZero', 'negative', 'negativeOrZero'];
        const selectedSigns = values.filter((v) => signKeys.includes(v));

        if (selectedSigns.length > 1) {
            return `Choose only one of: ${signKeys.join(', ')}`;
        }
    }

    return true;
}

async function fillStringValidation(validation, selected, field) {
    if (selected.includes('email')) {
        validation.email = true;
    }

    if (selected.includes('minLength')) {
        const { minLength } = await inquirer.prompt([
            {
                type: 'input',
                name: 'minLength',
                message: `Min length for "${field.name}":`,
                validate: (v) => {
                    const n = parseInt(v, 10);
                    return Number.isInteger(n) && n > 0 ? true : 'Must be a positive number.';
                },
                filter: (v) => parseInt(v, 10),
            },
        ]);

        validation.minLength = minLength;
    }

    if (selected.includes('maxLength')) {
        const { maxLength } = await inquirer.prompt([
            {
                type: 'input',
                name: 'maxLength',
                message: `Max length for "${field.name}":`,
                default: field.length ? String(field.length) : undefined,
                validate: (v) => {
                    const n = parseInt(v, 10);
                    return Number.isInteger(n) && n > 0 ? true : 'Must be a positive number.';
                },
                filter: (v) => parseInt(v, 10),
            },
        ]);

        validation.maxLength = maxLength;
    }

    if (selected.includes('pattern')) {
        const { pattern, patternMessage } = await inquirer.prompt([
            {
                type: 'input',
                name: 'pattern',
                message: `Regex pattern for "${field.name}":`,
                validate: (v) => {
                    const t = (v || '').trim();
                    if (!t) return 'Pattern is required.';
                    try {
                        // Best effort. Java regex may differ from JS regex.
                        // eslint-disable-next-line no-new
                        new RegExp(t);
                        return true;
                    } catch (err) {
                        return `Invalid regex: ${err.message}`;
                    }
                },
                filter: (v) => v.trim(),
            },
            {
                type: 'input',
                name: 'patternMessage',
                message: `Pattern error message for "${field.name}" (optional):`,
                filter: (v) => (v || '').trim(),
            },
        ]);

        validation.pattern = pattern;
        if (patternMessage) {
            validation.patternMessage = patternMessage;
        }
    }
}

async function fillIntegerValidation(validation, selected, field) {
    if (selected.includes('min')) {
        const { min } = await inquirer.prompt([
            {
                type: 'input',
                name: 'min',
                message: `Min value for "${field.name}":`,
                validate: (v) => Number.isInteger(Number(v)) ? true : 'Must be an integer.',
                filter: (v) => Number(v),
            },
        ]);

        validation.min = min;
    }

    if (selected.includes('max')) {
        const { max } = await inquirer.prompt([
            {
                type: 'input',
                name: 'max',
                message: `Max value for "${field.name}":`,
                validate: (v) => Number.isInteger(Number(v)) ? true : 'Must be an integer.',
                filter: (v) => Number(v),
            },
        ]);

        validation.max = max;
    }

    for (const key of ['positive', 'positiveOrZero', 'negative', 'negativeOrZero']) {
        if (selected.includes(key)) {
            validation[key] = true;
        }
    }
}

async function fillDecimalValidation(validation, selected, field) {
    if (selected.includes('decimalMin')) {
        const { decimalMin } = await inquirer.prompt([
            {
                type: 'input',
                name: 'decimalMin',
                message: `Decimal min for "${field.name}":`,
                validate: (v) => /^-?\d+(\.\d+)?$/.test(String(v).trim()) ? true : 'Must be a decimal number.',
                filter: (v) => String(v).trim(),
            },
        ]);

        validation.decimalMin = decimalMin;
    }

    if (selected.includes('decimalMax')) {
        const { decimalMax } = await inquirer.prompt([
            {
                type: 'input',
                name: 'decimalMax',
                message: `Decimal max for "${field.name}":`,
                validate: (v) => /^-?\d+(\.\d+)?$/.test(String(v).trim()) ? true : 'Must be a decimal number.',
                filter: (v) => String(v).trim(),
            },
        ]);

        validation.decimalMax = decimalMax;
    }

    if (selected.includes('digits')) {
        const { integer, fraction } = await inquirer.prompt([
            {
                type: 'input',
                name: 'integer',
                message: `Digits integer part for "${field.name}":`,
                validate: (v) => {
                    const n = parseInt(v, 10);
                    return Number.isInteger(n) && n > 0 ? true : 'Must be a positive number.';
                },
                filter: (v) => parseInt(v, 10),
            },
            {
                type: 'input',
                name: 'fraction',
                message: `Digits fraction part for "${field.name}":`,
                default: '2',
                validate: (v) => {
                    const n = parseInt(v, 10);
                    return Number.isInteger(n) && n >= 0 ? true : 'Must be zero or a positive number.';
                },
                filter: (v) => parseInt(v, 10),
            },
        ]);

        validation.digits = { integer, fraction };
    }

    for (const key of ['positive', 'positiveOrZero', 'negative', 'negativeOrZero']) {
        if (selected.includes(key)) {
            validation[key] = true;
        }
    }
}

async function promptDateValidation(field) {
    const { selected } = await inquirer.prompt([
        {
            type: 'list',
            name: 'selected',
            message: `Extra validation for "${field.name}":`,
            choices: [
                { name: 'None', value: null },
                { name: 'Past', value: 'past' },
                { name: 'Past Or Present', value: 'pastOrPresent' },
                { name: 'Future', value: 'future' },
                { name: 'Future Or Present', value: 'futureOrPresent' },
            ],
            default: null,
        },
    ]);

    if (!selected) {
        return {};
    }

    const validation = { [selected]: true };
    return normalizeValidationConfig(validation, field, `${field.name}.validation`);
}

async function promptBooleanValidation(field) {
    const { selected } = await inquirer.prompt([
        {
            type: 'list',
            name: 'selected',
            message: `Extra validation for "${field.name}":`,
            choices: [
                { name: 'None', value: null },
                { name: 'Assert True', value: 'assertTrue' },
                { name: 'Assert False', value: 'assertFalse' },
            ],
            default: null,
        },
    ]);

    if (!selected) {
        return {};
    }

    const validation = { [selected]: true };
    return normalizeValidationConfig(validation, field, `${field.name}.validation`);
}

function formatDtoFlags(dto) {
    if (!dto) {
        return '';
    }

    const included = [];

    if (dto.create) included.push('create');
    if (dto.update) included.push('update');
    if (dto.summary) included.push('summary');
    if (dto.detail) included.push('detail');

    return included.length
        ? ` DTO:${included.join(',')}`
        : ' DTO:none';
}

function formatValidationFlags(validation) {
    if (!validation || Object.keys(validation).length === 0) {
        return '';
    }

    const parts = [];

    for (const [key, value] of Object.entries(validation)) {
        if (key === 'digits' && value) {
            parts.push(`digits(${value.integer},${value.fraction})`);
        } else if (value === true) {
            parts.push(key);
        } else if (value !== false && value !== undefined && value !== null && value !== '') {
            parts.push(`${key}=${value}`);
        }
    }

    return parts.length
        ? ` validation:${parts.join(',')}`
        : '';
}

module.exports = resourceCreate;
