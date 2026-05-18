const path = require('path');
const _ = require('lodash');
const chalk = require('chalk');
const ora = require('ora');
const inquirer = require('inquirer');

const { assertProjectRoot, detectPluginFromCwd, getPluginModules, resolveCwd } = require('../plugin-utils');
const { generateUploader, resolvePluginPackage } = require('../uploader-generator');
const { validateEntityName } = require('../validators');

async function uploaderCreate(entityName, opts) {
    console.log(chalk.cyan.bold('\n  gasi - Uploader Generator\n'));

    if (!entityName) {
        throw new Error('Uploader name is required. Example: gasi uploader create Employee');
    }

    const entityResult = validateEntityName(entityName);
    if (entityResult !== true) {
        throw new Error(entityResult);
    }

    const { cwd, pluginModule, pluginDir } = await resolveTargetPlugin(opts);
    const resourceName = normalizeResourceName(opts.resource || _.kebabCase(entityName));
    const pluginName = path.basename(pluginModule).replace(/-plugin$/, '');

    console.log(chalk.bold('Summary:'));
    console.log(`  Plugin         : ${chalk.green(pluginModule)}`);
    console.log(`  Processor      : ${chalk.green(`${entityName}UplProcessor`)}`);
    console.log(`  Resource       : ${chalk.green(resourceName)}`);
    console.log();

    if (!opts.yes) {
        const { confirm } = await inquirer.prompt([
            { type: 'confirm', name: 'confirm', message: 'Generate uploader?', default: true },
        ]);

        if (!confirm) {
            console.log(chalk.yellow('Cancelled.'));
            return;
        }
    }

    const spinner = ora('Generating uploader files...').start();

    try {
        const generatedFiles = await generateUploader({
            pluginDir,
            entityName,
            resourceName,
        });

        spinner.succeed(`Generated ${generatedFiles.length} uploader file(s).`);
        console.log();

        for (const file of generatedFiles) {
            console.log(chalk.gray('    ' + path.relative(cwd, file)));
        }
    } catch (err) {
        spinner.fail('Failed to generate uploader.');
        throw err;
    }

    console.log(chalk.green.bold('\nUploader created successfully!\n'));
    console.log(chalk.bold('Next steps:'));
    console.log(chalk.gray('  1.') + ` Review ${entityName}UplProcessor parse, validateRows, and commitRows`);
    console.log(chalk.gray('  2.') + ` Build: ${chalk.cyan('gasi plugin build ' + pluginName)}`);
    console.log(chalk.gray('  3.') + ` Test upload API: ${chalk.cyan(`/api/v1/${resourceName}/upl`)}\n`);
}

async function resolveTargetPlugin(opts) {
    const detected = await detectPluginFromCwd(resolveCwd(opts));
    if (detected && !opts.plugin) {
        console.log(`  Plugin: ${chalk.green(detected.pluginModule)}\n`);
        return {
            cwd: detected.projectRoot,
            pluginModule: detected.pluginModule,
            pluginDir: detected.pluginDir,
        };
    }

    const cwd = detected ? detected.projectRoot : resolveCwd(opts);
    await assertProjectRoot(cwd);

    const pluginModules = await getPluginModules(cwd);
    if (!pluginModules.length) {
        throw new Error('No plugin modules found in the parent pom.xml. Create a plugin first with "gasi plugin create".');
    }

    const pluginChoices = [];
    for (const moduleName of pluginModules) {
        const moduleDir = path.join(cwd, moduleName);
        try {
            await resolvePluginPackage(moduleDir);
            pluginChoices.push(moduleName);
        } catch (err) {
            // Ignore invalid plugin modules in the picker.
        }
    }

    if (!pluginChoices.length) {
        throw new Error('No valid plugin modules found. Ensure plugin.properties exists in each plugin resources directory.');
    }

    let pluginModule;
    if (opts.plugin) {
        const normalized = normalizePluginModule(opts.plugin);
        pluginModule = pluginChoices.find((choice) => choice === normalized);
        if (!pluginModule) {
            throw new Error(`Plugin module '${normalized}' is not registered or is not valid.`);
        }
    } else {
        const answer = await inquirer.prompt([
            {
                type: 'list',
                name: 'pluginModule',
                message: 'Target plugin:',
                choices: pluginChoices,
            },
        ]);
        pluginModule = answer.pluginModule;
    }

    return {
        cwd,
        pluginModule,
        pluginDir: path.join(cwd, pluginModule),
    };
}

function normalizePluginModule(plugin) {
    const raw = plugin.trim();
    if (raw.startsWith('plugins/')) {
        return raw.endsWith('-plugin') ? raw : `${raw}-plugin`;
    }
    return `plugins/${raw.endsWith('-plugin') ? raw : `${raw}-plugin`}`;
}

function normalizeResourceName(resourceName) {
    const value = (resourceName || '').trim().toLowerCase();
    if (!value) {
        throw new Error('Resource name is required.');
    }
    if (!/^[a-z][a-z0-9-]*$/.test(value)) {
        throw new Error('Resource name must use lowercase letters, numbers, and dashes only. Example: employee');
    }
    return value;
}

module.exports = uploaderCreate;
