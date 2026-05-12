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

async function resolvePluginMetadata(pluginDir) {
    const propsFile = path.join(pluginDir, 'src', 'main', 'resources', 'plugin.properties');

    if (!(await fs.pathExists(propsFile))) {
        return null;
    }

    const content = await fs.readFile(propsFile, 'utf8');

    const classMatch = content.match(/^plugin\.class\s*=\s*(.+)$/m);
    const prefixMatch = content.match(/^plugin\.prefix\s*=\s*(.+)$/m);

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
        pluginPrefix: prefixMatch ? prefixMatch[1].trim() : null,
        propsFile,
    };
}

async function resourceCreate(entityName, opts) {
    console.log(chalk.cyan.bold('\n  gasi — Resource Generator\n'));

    const result = validateEntityName(entityName);
    if (result !== true) throw new Error(result);

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

    if (!pluginMetadata || !pluginMetadata.pluginPrefix) {
        const propsFile = path.join(pluginDir, 'src', 'main', 'resources', 'plugin.properties');
        throw new Error(`plugin.prefix is required in ${propsFile}`);
    }

    const pluginPrefix = pluginMetadata.pluginPrefix;

    // ── 2. Collect fields ──────────────────────────────────────────────
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

    // ── 3. Summary & confirm ───────────────────────────────────────────
    const tableName = pluralize(_.snakeCase(entityName));

    console.log('\n' + chalk.bold('Summary:'));
    console.log(`  Plugin         : ${chalk.green(pluginModule)}`);
    console.log(`  Prefix         : ${chalk.green(pluginPrefix)}`);
    console.log(`  Entity         : ${chalk.green(entityName)}`);
    console.log(`  Table          : ${chalk.green(tableName)}`);
    console.log(`  Fields         :`);
    for (const f of fields) {
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
        const generatedFiles = await generateResource({
            cwd,
            pluginDir,
            pluginName,
            pluginPrefix,
            entityName,
            tableName,
            fields,
        });

        spinner.succeed(`Generated ${generatedFiles.length} files.`);
        console.log();
        for (const f of generatedFiles) {
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
        { type: 'confirm', name: 'filterable', message: `Filterable?`, default: false }
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
