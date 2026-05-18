/**
 * resource-templates.js
 *
 * Builds the template context (token values) for resource generation.
 * Actual templates live in templates/resource/ as plain files.
 *
 * This module computes the dynamic parts — field declarations, imports,
 * annotations, SQL columns — that get injected into the template tokens.
 */
const _ = require('lodash');
const pluralize = require('pluralize');

const TYPE_MAP = {
    String: { javaType: 'String', javaImports: [], sqlType: (f) => `VARCHAR(${f.length || 255})` },
    Text: { javaType: 'String', javaImports: [], sqlType: () => 'TEXT' },
    MediumText: { javaType: 'String', javaImports: [], sqlType: () => 'MEDIUMTEXT' },
    Integer: { javaType: 'Integer', javaImports: [], sqlType: () => 'INT' },
    Long: { javaType: 'Long', javaImports: [], sqlType: () => 'BIGINT' },
    BigDecimal: { javaType: 'BigDecimal', javaImports: ['java.math.BigDecimal'], sqlType: () => 'DECIMAL(19,4)' },
    Double: { javaType: 'Double', javaImports: [], sqlType: () => 'DOUBLE' },
    Boolean: { javaType: 'Boolean', javaImports: [], sqlType: () => 'BOOLEAN' },
    Date: { javaType: 'LocalDate', javaImports: ['java.time.LocalDate'], sqlType: () => 'DATE' },
    DateTime: { javaType: 'LocalDateTime', javaImports: ['java.time.LocalDateTime'], sqlType: () => 'DATETIME(6)' },
    Instant: { javaType: 'Instant', javaImports: ['java.time.Instant'], sqlType: () => 'TIMESTAMP(6)' },
    Enum: { javaType: null, javaImports: [], sqlType: () => 'VARCHAR(50)' },
    ManyToOne: { javaType: null, javaImports: [], sqlType: () => 'BIGINT' },
};

const STRING_TYPES = new Set(['String', 'Text', 'MediumText']);
const INTEGER_TYPES = new Set(['Integer', 'Long']);
const DECIMAL_TYPES = new Set(['BigDecimal', 'Double']);
const DATE_TYPES = new Set(['Date', 'DateTime', 'Instant']);

// ─── Helpers ───────────────────────────────────────────────────────────

function javaType(field) {
    if (field.type === 'Enum') return field.enumName;
    if (field.type === 'ManyToOne') return field.refEntity;
    return TYPE_MAP[field.type].javaType;
}

function dtoJavaType(field) {
    if (field.type === 'ManyToOne') return 'String';
    if (field.type === 'Enum') return field.enumName;
    return TYPE_MAP[field.type].javaType;
}

function collectImports(fields) {
    const imports = new Set();
    for (const f of fields) {
        const mapping = TYPE_MAP[f.type];
        if (mapping) {
            mapping.javaImports.forEach((i) => imports.add(i));
        }
    }
    return [...imports].sort();
}

function formatImports(importList) {
    if (!importList.length) return '';
    return importList.map((i) => `import ${i};`).join('\n') + '\n';
}

function isIncluded(field, dtoName) {
    return !field.dto || field.dto[dtoName] !== false;
}

function fieldsForDto(fields, dtoName) {
    return fields.filter((f) => isIncluded(f, dtoName));
}

function escapeJavaString(value) {
    return String(value)
        .replace(/\\/g, '\\\\')
        .replace(/"/g, '\\"');
}

function buildTableName(pluginPrefix, entityName) {
    const baseTableName = pluralize(_.snakeCase(entityName));
    return pluginPrefix ? `${pluginPrefix}_${baseTableName}` : baseTableName;
}

// ─── Field Renderers ───────────────────────────────────────────────────

function renderDomainModelFields(fields) {
    return fields.map((f) => {
        if (f.type === 'ManyToOne') {
            return `    private ${f.refEntity} ${f.name};`;
        }
        return `    private ${javaType(f)} ${f.name};`;
    }).join('\n');
}

function renderDtoFields(fields) {
    return fields.map((f) => {
        const annotations = renderDtoValidationAnnotations(f);
        const annotStr = annotations.length ? annotations.join('\n') + '\n' : '';
        const fieldName = f.type === 'ManyToOne' ? f.name + 'Id' : f.name;
        return `${annotStr}    private ${dtoJavaType(f)} ${fieldName};`;
    }).join('\n\n');
}

function renderDtoValidationAnnotations(field) {
    const annotations = [];
    const validation = field.validation || {};

    if (field.required) {
        if (STRING_TYPES.has(field.type)) {
            annotations.push('    @NotBlank');
        } else {
            annotations.push('    @NotNull');
        }
    }

    if (STRING_TYPES.has(field.type)) {
        const sizeParts = [];
        if (validation.minLength !== undefined) sizeParts.push(`min = ${validation.minLength}`);

        const maxLength = validation.maxLength !== undefined
            ? validation.maxLength
            : field.type === 'String' && field.length
                ? field.length
                : undefined;

        if (maxLength !== undefined) sizeParts.push(`max = ${maxLength}`);
        if (sizeParts.length) annotations.push(`    @Size(${sizeParts.join(', ')})`);

        if (validation.email) annotations.push('    @Email');
        if (validation.pattern) {
            const attrs = [`regexp = "${escapeJavaString(validation.pattern)}"`];
            if (validation.patternMessage) attrs.push(`message = "${escapeJavaString(validation.patternMessage)}"`);
            annotations.push(`    @Pattern(${attrs.join(', ')})`);
        }
    }

    if (INTEGER_TYPES.has(field.type)) {
        if (validation.min !== undefined) annotations.push(`    @Min(${validation.min})`);
        if (validation.max !== undefined) annotations.push(`    @Max(${validation.max})`);
    }

    if (DECIMAL_TYPES.has(field.type)) {
        if (validation.decimalMin !== undefined) annotations.push(`    @DecimalMin("${escapeJavaString(validation.decimalMin)}")`);
        if (validation.decimalMax !== undefined) annotations.push(`    @DecimalMax("${escapeJavaString(validation.decimalMax)}")`);
        if (validation.digits) annotations.push(`    @Digits(integer = ${validation.digits.integer}, fraction = ${validation.digits.fraction})`);
    }

    if (INTEGER_TYPES.has(field.type) || DECIMAL_TYPES.has(field.type)) {
        if (validation.positive) annotations.push('    @Positive');
        if (validation.positiveOrZero) annotations.push('    @PositiveOrZero');
        if (validation.negative) annotations.push('    @Negative');
        if (validation.negativeOrZero) annotations.push('    @NegativeOrZero');
    }

    if (DATE_TYPES.has(field.type)) {
        if (validation.past) annotations.push('    @Past');
        if (validation.pastOrPresent) annotations.push('    @PastOrPresent');
        if (validation.future) annotations.push('    @Future');
        if (validation.futureOrPresent) annotations.push('    @FutureOrPresent');
    }

    if (field.type === 'Boolean') {
        if (validation.assertTrue) annotations.push('    @AssertTrue');
        if (validation.assertFalse) annotations.push('    @AssertFalse');
    }

    return annotations;
}

function renderDtoImports(fields) {
    const imports = new Set();

    for (const f of fields) {
        const validation = f.validation || {};

        if (f.required) {
            if (STRING_TYPES.has(f.type)) imports.add('jakarta.validation.constraints.NotBlank');
            else imports.add('jakarta.validation.constraints.NotNull');
        }

        const hasSize = STRING_TYPES.has(f.type) && (
            validation.minLength !== undefined ||
            validation.maxLength !== undefined ||
            (f.type === 'String' && f.length)
        );
        if (hasSize) imports.add('jakarta.validation.constraints.Size');

        if (validation.email) imports.add('jakarta.validation.constraints.Email');
        if (validation.pattern) imports.add('jakarta.validation.constraints.Pattern');

        if (validation.min !== undefined) imports.add('jakarta.validation.constraints.Min');
        if (validation.max !== undefined) imports.add('jakarta.validation.constraints.Max');

        if (validation.decimalMin !== undefined) imports.add('jakarta.validation.constraints.DecimalMin');
        if (validation.decimalMax !== undefined) imports.add('jakarta.validation.constraints.DecimalMax');
        if (validation.digits) imports.add('jakarta.validation.constraints.Digits');

        if (validation.positive) imports.add('jakarta.validation.constraints.Positive');
        if (validation.positiveOrZero) imports.add('jakarta.validation.constraints.PositiveOrZero');
        if (validation.negative) imports.add('jakarta.validation.constraints.Negative');
        if (validation.negativeOrZero) imports.add('jakarta.validation.constraints.NegativeOrZero');

        if (validation.past) imports.add('jakarta.validation.constraints.Past');
        if (validation.pastOrPresent) imports.add('jakarta.validation.constraints.PastOrPresent');
        if (validation.future) imports.add('jakarta.validation.constraints.Future');
        if (validation.futureOrPresent) imports.add('jakarta.validation.constraints.FutureOrPresent');

        if (validation.assertTrue) imports.add('jakarta.validation.constraints.AssertTrue');
        if (validation.assertFalse) imports.add('jakarta.validation.constraints.AssertFalse');
    }

    collectImports(fields).forEach((i) => imports.add(i));

    return imports.size ? [...imports].sort().map((i) => `import ${i};`).join('\n') + '\n' : '';
}

function renderResponseFields(fields) {
    return fields.map((f) => {
        if (f.type === 'ManyToOne') {
            return `    private String ${f.name}Id;`;
        }
        return `    private ${javaType(f)} ${f.name};`;
    }).join('\n');
}

function renderResponseImports(fields) {
    const typeImports = collectImports(fields);
    return formatImports(typeImports);
}

function renderEntityFields(fields) {
    const lines = [];
    for (const f of fields) {
        const annotations = [];

        if (f.filterable) {
            annotations.push('    @Filterable');
        }

        if (f.type === 'ManyToOne') {
            const fkColumn = _.snakeCase(f.name) + '_id';
            annotations.push(`    @ManyToOne(fetch = FetchType.LAZY)`);
            annotations.push(`    @JoinColumn(name = "${fkColumn}"${f.required ? ', nullable = false' : ''})`);
            lines.push(annotations.join('\n') + `\n    private ${f.refEntity}Entity ${f.name};`);
            continue;
        }

        const column = _.snakeCase(f.name);
        const colAttrs = [`name = "${column}"`];
        if (f.required) colAttrs.push('nullable = false');
        if (f.unique) colAttrs.push('unique = true');
        if (f.type === 'String' && f.length) colAttrs.push(`length = ${f.length}`);
        if (f.type === 'Text') colAttrs.push('columnDefinition = "TEXT"');
        if (f.type === 'MediumText') colAttrs.push('columnDefinition = "MEDIUMTEXT"');

        annotations.push(`    @Column(${colAttrs.join(', ')})`);

        if (f.type === 'Text' || f.type === 'MediumText') {
            annotations.push('    @Lob');
        }
        if (f.type === 'Enum') {
            annotations.push('    @Enumerated(EnumType.ORDINAL)');
        }

        const jType = f.type === 'Enum' ? f.enumName : TYPE_MAP[f.type].javaType;
        lines.push(annotations.join('\n') + `\n    private ${jType} ${f.name};`);
    }
    return lines.join('\n\n');
}

function renderEntityImports(fields) {
    const imports = new Set();
    for (const f of fields) {
        if (f.filterable) imports.add('gasi.gps.core.starter.infrastructure.filter.Filterable');
        if (f.type === 'ManyToOne') {
            imports.add('jakarta.persistence.ManyToOne');
            imports.add('jakarta.persistence.JoinColumn');
            imports.add('jakarta.persistence.FetchType');
        } else {
            imports.add('jakarta.persistence.Column');
            if (f.type === 'Text' || f.type === 'MediumText') imports.add('jakarta.persistence.Lob');
            if (f.type === 'Enum') {
                imports.add('jakarta.persistence.Enumerated');
                imports.add('jakarta.persistence.EnumType');
            }
            const mapping = TYPE_MAP[f.type];
            if (mapping) mapping.javaImports.forEach((i) => imports.add(i));
        }
    }
    return [...imports].sort().map((i) => `import ${i};`).join('\n') + '\n';
}

function renderDtoMapperMappings({ createFields, updateFields, summaryFields, detailFields }) {
    const createM2oFields = createFields.filter((f) => f.type === 'ManyToOne');
    const updateM2oFields = updateFields.filter((f) => f.type === 'ManyToOne');
    const summaryM2oFields = summaryFields.filter((f) => f.type === 'ManyToOne');
    const detailM2oFields = detailFields.filter((f) => f.type === 'ManyToOne');

    const toModelMappings = createM2oFields.map((f) =>
        `    @Mapping(target = "${f.name}", ignore = true)`,
    ).join('\n');

    const updateModelMappings = updateM2oFields.map((f) =>
        `    @Mapping(target = "${f.name}", ignore = true)`,
    ).join('\n');

    const summaryMappings = summaryM2oFields.map((f) =>
        `    @Mapping(source = "${f.name}.id", target = "${f.name}Id", qualifiedByName = "encodeId")`,
    ).join('\n');

    const detailMappings = detailM2oFields.map((f) =>
        `    @Mapping(source = "${f.name}.id", target = "${f.name}Id", qualifiedByName = "encodeId")`,
    ).join('\n');

    const hasMappings = toModelMappings || updateModelMappings || summaryMappings || detailMappings;
    const needsIdEncoderField = false;

    return {
        extraImports: hasMappings ? 'import org.mapstruct.Mapping;\n' : '',
        autowiredImport: needsIdEncoderField ? 'import org.springframework.beans.factory.annotation.Autowired;\n' : '',
        idEncoderField: needsIdEncoderField ? '    @Autowired\n    protected IdEncoder idEncoder;\n' : '',
        toModelMappings: toModelMappings ? toModelMappings + '\n' : '',
        updateModelMappings: updateModelMappings ? updateModelMappings + '\n' : '',
        summaryMappings: summaryMappings ? summaryMappings + '\n' : '',
        detailMappings: detailMappings ? detailMappings + '\n' : '',
    };
}

function renderMigrationColumns(fields) {
    return fields.map((f) => {
        const mapping = TYPE_MAP[f.type];
        let colName, colType;

        if (f.type === 'ManyToOne') {
            colName = _.snakeCase(f.name) + '_id';
            colType = mapping.sqlType();
        } else {
            colName = _.snakeCase(f.name);
            colType = mapping.sqlType(f);
        }

        const nullable = f.required ? ' NOT NULL' : '';
        const unique = f.unique ? ' UNIQUE' : '';
        return `    ${colName} ${colType}${nullable}${unique},`;
    }).join('\n');
}

function renderMigrationFkConstraints(fields, tableName, pluginPrefix) {
    const fks = fields.filter((f) => f.type === 'ManyToOne');
    if (!fks.length) return '';

    return '\n' + fks.map((f) => {
        const fkCol = _.snakeCase(f.name) + '_id';
        const refTable = buildTableName(pluginPrefix, f.refEntity);
        return `ALTER TABLE ${tableName}\n    ADD CONSTRAINT fk_${tableName}_${fkCol}\n    FOREIGN KEY (${fkCol}) REFERENCES ${refTable}(id);`;
    }).join('\n');
}

function renderServiceContext({ pkg, entityName, fields }) {
    const createM2oFields = fieldsForDto(fields, 'create').filter((f) => f.type === 'ManyToOne');
    const updateM2oFields = fieldsForDto(fields, 'update').filter((f) => f.type === 'ManyToOne');
    const allM2oFields = uniqueManyToOneFields([...createM2oFields, ...updateM2oFields]);
    const hasRefs = allM2oFields.length > 0;

    const imports = new Set([
        'org.springframework.stereotype.Service',
        'gasi.gps.core.starter.infrastructure.i18n.MessageUtil',
        'gasi.gps.core.starter.infrastructure.util.IdEncoder',
    ]);

    if (hasRefs) {
        imports.add('gasi.gps.core.api.application.exception.BusinessException');
        imports.add('gasi.gps.core.starter.application.support.ReferenceResolver');
    }

    for (const field of allM2oFields) {
        imports.add(`${pkg}.domain.model.${field.refEntity}`);
        imports.add(`${pkg}.domain.port.outbound.${field.refEntity}RepositoryPort`);
    }

    return {
        imports: [...imports].sort().map((i) => `import ${i};`).join('\n') + '\n',
        fields: renderServiceFields(entityName, allM2oFields, hasRefs),
        constructorParams: renderServiceConstructorParams(entityName, allM2oFields, hasRefs),
        constructorAssignments: renderServiceConstructorAssignments(entityName, allM2oFields, hasRefs),
        referenceMethods: hasRefs
            ? renderServiceReferenceMethods({ entityName, createM2oFields, updateM2oFields, allM2oFields })
            : '',
    };
}

function uniqueManyToOneFields(fields) {
    const byName = new Map();
    for (const field of fields) {
        byName.set(field.name, field);
    }
    return [...byName.values()];
}

function renderServiceFields(entityName, allM2oFields, hasRefs) {
    if (!hasRefs) {
        return '';
    }

    const lines = [
        '    private final ReferenceResolver referenceResolver;',
        ...uniqueRefEntities(allM2oFields).map((refEntity) =>
            `    private final ${refEntity}RepositoryPort ${_.lowerFirst(refEntity)}RepositoryPort;`),
    ];

    return lines.join('\n') + '\n\n';
}

function renderServiceConstructorParams(entityName, allM2oFields, hasRefs) {
    const params = [
        `${entityName}RepositoryPort repositoryPort`,
        `${entityName}DtoMapper dtoMapper`,
        'MessageUtil messageUtil',
        'IdEncoder idEncoder',
    ];

    if (hasRefs) {
        params.push('ReferenceResolver referenceResolver');
        uniqueRefEntities(allM2oFields).forEach((refEntity) => {
            params.push(`${refEntity}RepositoryPort ${_.lowerFirst(refEntity)}RepositoryPort`);
        });
    }

    return params.join(',\n            ');
}

function renderServiceConstructorAssignments(entityName, allM2oFields, hasRefs) {
    if (!hasRefs) {
        return '';
    }

    const lines = [
        '        this.referenceResolver = referenceResolver;',
        ...uniqueRefEntities(allM2oFields).map((refEntity) =>
            `        this.${_.lowerFirst(refEntity)}RepositoryPort = ${_.lowerFirst(refEntity)}RepositoryPort;`),
    ];

    return lines.join('\n') + '\n';
}

function renderServiceReferenceMethods({ entityName, createM2oFields, updateM2oFields, allM2oFields }) {
    const entityVar = _.lowerFirst(entityName);
    return `
    @Override
    protected void beforeCreate(${entityName} ${entityVar}, ${entityName}CreateRequest request) {
        BusinessException.Collector collector = new BusinessException.Collector();
${renderResolveAssignments(createM2oFields)}
        collector.validate();
${renderApplyRefs(entityVar, createM2oFields)}
    }

    @Override
    protected void beforeUpdate(${entityName} ${entityVar}, ${entityName}UpdateRequest request) {
        BusinessException.Collector collector = new BusinessException.Collector();
${renderResolveAssignments(updateM2oFields)}
        collector.validate();
${renderApplyRefs(entityVar, updateM2oFields)}
    }
`;
}

function renderResolveAssignments(fields) {
    if (!fields.length) {
        return '';
    }
    return fields.map((field) => `        ${field.refEntity} ${field.name} = referenceResolver.resolve(
                ${_.lowerFirst(field.refEntity)}RepositoryPort,
                request.get${_.upperFirst(field.name)}Id(),
                "${field.name}Id",
                collector);`).join('\n');
}

function renderApplyRefs(entityVar, fields) {
    if (!fields.length) {
        return '';
    }
    return fields.map((field) => `        ${entityVar}.set${_.upperFirst(field.name)}(${field.name});`).join('\n');
}

function uniqueRefEntities(fields) {
    return [...new Set(fields.map((field) => field.refEntity))];
}

// ─── Main Context Builder ──────────────────────────────────────────────

/**
 * Build the full token context for template rendering.
 *
 * @param {object} params - { pkg, entityName, tableName, fields, pluginName, domain }
 * @returns {object} context with all tokens needed by templates/resource/
 */
function buildResourceContext({ pkg, entityName, tableName, fields, pluginName, pluginPrefix, domain }) {
    const entityVar = _.lowerFirst(entityName);
    const apiPath = pluralize(_.snakeCase(entityName).replace(/_/g, '-'));
    // PKG_PATH is base package only (e.g. "gasi/gps"), DOMAIN is appended via [[DOMAIN]] in template path
    const basePkg = pkg.substring(0, pkg.lastIndexOf('.'));
    const pkgPath = basePkg.replace(/\./g, '/');

    const createFields = fieldsForDto(fields, 'create');
    const updateFields = fieldsForDto(fields, 'update');
    const summaryFields = fieldsForDto(fields, 'summary');
    const detailFields = fieldsForDto(fields, 'detail');

    const mapperCtx = renderDtoMapperMappings({
        createFields,
        updateFields,
        summaryFields,
        detailFields,
    });
    const serviceCtx = renderServiceContext({ pkg, entityName, fields });

    return {
        // Path tokens ([[TOKEN]] in directory/file names)
        PKG_PATH: pkgPath,
        DOMAIN: domain,
        ENTITY_NAME: entityName,
        TABLE_NAME: tableName,
        PLUGIN_NAME: pluginName,
        PLUGIN_PREFIX: pluginPrefix,
        MIGRATION_TIMESTAMP: nowTimestamp(),

        // Content tokens ({{TOKEN}} inside file contents)
        FULL_PACKAGE: pkg,
        ENTITY_VAR: entityVar,
        API_PATH: apiPath,

        // Domain model
        DOMAIN_MODEL_IMPORTS: formatImports(collectImports(fields)),
        DOMAIN_MODEL_FIELDS: renderDomainModelFields(fields),

        // DTOs — request
        CREATE_REQUEST_IMPORTS: renderDtoImports(createFields),
        CREATE_REQUEST_FIELDS: renderDtoFields(createFields),
        UPDATE_REQUEST_IMPORTS: renderDtoImports(updateFields),
        UPDATE_REQUEST_FIELDS: renderDtoFields(updateFields),

        // DTOs — response
        SUMMARY_RESPONSE_IMPORTS: renderResponseImports(summaryFields),
        SUMMARY_RESPONSE_FIELDS: renderResponseFields(summaryFields),
        DETAIL_RESPONSE_IMPORTS: renderResponseImports(detailFields),
        DETAIL_RESPONSE_FIELDS: renderResponseFields(detailFields),

        // DTO Mapper
        DTO_MAPPER_EXTRA_IMPORTS: mapperCtx.extraImports,
        DTO_MAPPER_AUTOWIRED_IMPORT: mapperCtx.autowiredImport,
        DTO_MAPPER_ID_ENCODER_FIELD: mapperCtx.idEncoderField,
        DTO_MAPPER_TO_MODEL_MAPPINGS: mapperCtx.toModelMappings,
        DTO_MAPPER_UPDATE_MODEL_MAPPINGS: mapperCtx.updateModelMappings,
        DTO_MAPPER_SUMMARY_MAPPINGS: mapperCtx.summaryMappings,
        DTO_MAPPER_DETAIL_MAPPINGS: mapperCtx.detailMappings,
        DTO_MAPPER_CHILD_IMPORTS: '',
        DTO_MAPPER_CHILD_METHODS: '',

        // Service
        SERVICE_IMPORTS: serviceCtx.imports,
        SERVICE_FIELDS: serviceCtx.fields,
        SERVICE_CONSTRUCTOR_PARAMS: serviceCtx.constructorParams,
        SERVICE_CONSTRUCTOR_ASSIGNMENTS: serviceCtx.constructorAssignments,
        SERVICE_REFERENCE_METHODS: serviceCtx.referenceMethods,

        // JPA Entity
        ENTITY_IMPORTS: renderEntityImports(fields),
        ENTITY_FIELDS: renderEntityFields(fields),

        // Migration
        MIGRATION_COLUMNS: renderMigrationColumns(fields),
        MIGRATION_FK_CONSTRAINTS: renderMigrationFkConstraints(fields, tableName, pluginPrefix),
    };
}

function nowTimestamp() {
    const d = new Date();
    const pad = (n) => String(n).padStart(2, '0');
    return (
        d.getFullYear().toString() +
        pad(d.getMonth() + 1) +
        pad(d.getDate()) +
        pad(d.getHours()) +
        pad(d.getMinutes()) +
        pad(d.getSeconds())
    );
}

module.exports = { buildResourceContext, TYPE_MAP };
