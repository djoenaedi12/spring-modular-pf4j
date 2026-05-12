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

/**
 * Field‐type choices shown in the interactive prompt.
 *
 * "value" is the canonical key used by the generator;
 * the mapping to a concrete Java class lives in resource-templates.js
 */
const FIELD_TYPES = [
    { name: 'String', value: 'String' },
    { name: 'Text', value: 'Text' },

    // Optional kalau generator kamu sudah support MEDIUMTEXT.
    // Kalau belum support di resource-templates.js / migration generator,
    // hapus line ini dulu.
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

        fields.push(field);
    }

    return {
        entityName: finalEntityName,
        fields,
    };
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
    console.log(`  Prefix         : ${chalk.green(pluginPrefixRaw)}`);
    console.log(`  Resources      : ${chalk.green(resourceSpecs.length)}`);

    for (const resource of resourceSpecs) {
        const tableName = buildTableName(pluginPrefix, resource.entityName);

        console.log();
        console.log(`  Entity         : ${chalk.green(resource.entityName)}`);
        console.log(`  Table          : ${chalk.green(tableName)}`);

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
            console.log(`    - ${chalk.cyan(f.name)} ${chalk.yellow(typeLabel)}${flagStr}`);
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

            const generatedFiles = await generateResource({
                cwd,
                pluginDir,
                pluginName,
                pluginPrefix,
                pluginPrefixRaw,
                entityName: resource.entityName,
                tableName,
                fields: resource.fields,
            });

            allGeneratedFiles.push(...generatedFiles);
        }

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

    return field;
}

module.exports = resourceCreate;
