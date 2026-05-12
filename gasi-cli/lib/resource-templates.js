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
        const annotations = [];
        if (f.required) {
            if (f.type === 'String' || f.type === 'Text') {
                annotations.push('    @NotBlank');
            } else {
                annotations.push('    @NotNull');
            }
        }
        if (f.type === 'String' && f.length) {
            annotations.push(`    @Size(max = ${f.length})`);
        }
        const annotStr = annotations.length ? annotations.join('\n') + '\n' : '';
        const fieldName = f.type === 'ManyToOne' ? f.name + 'Id' : f.name;
        return `${annotStr}    private ${dtoJavaType(f)} ${fieldName};`;
    }).join('\n\n');
}

function renderDtoImports(fields) {
    const imports = [];
    const hasNotBlank = fields.some((f) => f.required && (f.type === 'String' || f.type === 'Text'));
    const hasNotNull = fields.some((f) => f.required && f.type !== 'String' && f.type !== 'Text');
    const hasSize = fields.some((f) => f.type === 'String' && f.length);

    if (hasNotBlank) imports.push('import jakarta.validation.constraints.NotBlank;');
    if (hasNotNull) imports.push('import jakarta.validation.constraints.NotNull;');
    if (hasSize) imports.push('import jakarta.validation.constraints.Size;');

    const typeImports = collectImports(fields);
    typeImports.forEach((i) => imports.push(`import ${i};`));

    return imports.length ? imports.join('\n') + '\n' : '';
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

        annotations.push(`    @Column(${colAttrs.join(', ')})`);

        if (f.type === 'Text') {
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
        if (f.filterable) imports.add('gasi.gps.core.starter.infrastructure.filter.Filterable;');
        if (f.type === 'ManyToOne') {
            imports.add('jakarta.persistence.ManyToOne');
            imports.add('jakarta.persistence.JoinColumn');
            imports.add('jakarta.persistence.FetchType');
        } else {
            imports.add('jakarta.persistence.Column');
            if (f.type === 'Text') imports.add('jakarta.persistence.Lob');
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

function renderDtoMapperMappings(fields) {
    const m2oFields = fields.filter((f) => f.type === 'ManyToOne');

    const toModelMappings = m2oFields.map((f) =>
        `    @Mapping(target = "${f.name}", ignore = true)`,
    ).join('\n');

    const responseMappings = m2oFields.map((f) =>
        `    @Mapping(source = "${f.name}.id", target = "${f.name}Id", qualifiedByName = "encodeId")`,
    ).join('\n');

    return {
        extraImports: m2oFields.length ? 'import org.mapstruct.Mapping;\n' : '',
        toModelMappings: toModelMappings ? toModelMappings + '\n' : '',
        updateModelMappings: toModelMappings ? toModelMappings + '\n' : '',
        summaryMappings: responseMappings ? responseMappings + '\n' : '',
        detailMappings: responseMappings ? responseMappings + '\n' : '',
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

function renderMigrationFkConstraints(fields, tableName) {
    const fks = fields.filter((f) => f.type === 'ManyToOne');
    if (!fks.length) return '';

    return '\n' + fks.map((f) => {
        const fkCol = _.snakeCase(f.name) + '_id';
        const refTable = pluralize(_.snakeCase(f.refEntity));
        return `ALTER TABLE ${tableName}\n    ADD CONSTRAINT fk_${tableName}_${fkCol}\n    FOREIGN KEY (${fkCol}) REFERENCES ${refTable}(id);`;
    }).join('\n');
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

    const mapperCtx = renderDtoMapperMappings(fields);

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
        CREATE_REQUEST_IMPORTS: renderDtoImports(fields),
        CREATE_REQUEST_FIELDS: renderDtoFields(fields),
        UPDATE_REQUEST_IMPORTS: renderDtoImports(fields),
        UPDATE_REQUEST_FIELDS: renderDtoFields(fields),

        // DTOs — response
        SUMMARY_RESPONSE_IMPORTS: renderResponseImports(fields),
        SUMMARY_RESPONSE_FIELDS: renderResponseFields(fields),
        DETAIL_RESPONSE_IMPORTS: renderResponseImports(fields),
        DETAIL_RESPONSE_FIELDS: renderResponseFields(fields),

        // DTO Mapper
        DTO_MAPPER_EXTRA_IMPORTS: mapperCtx.extraImports,
        DTO_MAPPER_TO_MODEL_MAPPINGS: mapperCtx.toModelMappings,
        DTO_MAPPER_UPDATE_MODEL_MAPPINGS: mapperCtx.updateModelMappings,
        DTO_MAPPER_SUMMARY_MAPPINGS: mapperCtx.summaryMappings,
        DTO_MAPPER_DETAIL_MAPPINGS: mapperCtx.detailMappings,

        // JPA Entity
        ENTITY_IMPORTS: renderEntityImports(fields),
        ENTITY_FIELDS: renderEntityFields(fields),

        // Migration
        MIGRATION_COLUMNS: renderMigrationColumns(fields),
        MIGRATION_FK_CONSTRAINTS: renderMigrationFkConstraints(fields, tableName),
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
