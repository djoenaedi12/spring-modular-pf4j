const _ = require('lodash');
const path = require('path');
const fs = require('fs-extra');
const chalk = require('chalk');
const ora = require('ora');
const inquirer = require('inquirer');

const { renderTemplateTree } = require('../template-engine');
const { registerInParentPom } = require('../pom-registrar');
const { listPluginModules } = require('../pom-scanner');
const {
    validatePluginName,
    validatePackage,
    validateDomain,
    validatePluginDependency,
} = require('../validators');

async function pluginCreate(opts) {
    const cwd = opts.cwd ? path.resolve(opts.cwd) : process.cwd();

    console.log(chalk.cyan.bold('\n  gasi — Plugin Generator\n'));

    // Verify there's a parent pom (we don't assume any specific project name yet)
    const parentPom = path.join(cwd, 'pom.xml');
    if (!(await fs.pathExists(parentPom))) {
        throw new Error(`No pom.xml found in ${cwd}. Run this from the project root or use --cwd.`);
    }

    // Collect answers — from flags or interactive prompts
    const answers = await collectAnswers(opts, parentPom);

    // Confirmation
    const targetDir = path.join(cwd, 'plugins', `${answers.name}-plugin`);
    if (await fs.pathExists(targetDir)) {
        throw new Error(`Target directory already exists: ${targetDir}`);
    }

    if (!opts.yes) {
        console.log('\n' + chalk.bold('Summary:'));
        console.log(`  Plugin name     : ${chalk.green(answers.name + '-plugin')}`);
        console.log(`  Plugin prefix   : ${chalk.green(answers.prefix)}`);
        console.log(`  Domain          : ${chalk.green(answers.domain)}`);
        console.log(`  Base package    : ${chalk.green(answers.basePackage)}`);
        console.log(`  Full package    : ${chalk.green(answers.basePackage + '.' + answers.domain)}`);
        console.log(`  Version         : ${chalk.green(answers.version)}`);
        console.log(`  Description     : ${chalk.green(answers.description)}`);
        console.log(`  Depends on      : ${answers.dependsOn.length ? chalk.green(answers.dependsOn.join(', ')) : chalk.gray('(none)')}`);
        console.log(`  Flyway sample   : ${answers.flyway ? chalk.green('yes') : chalk.gray('no')}`);
        console.log(`  Auto-register   : ${answers.register ? chalk.green('yes') : chalk.gray('no')}`);
        console.log(`  Target          : ${chalk.gray(targetDir)}\n`);

        const { confirm } = await inquirer.prompt([
            { type: 'confirm', name: 'confirm', message: 'Continue?', default: true },
        ]);
        if (!confirm) {
            console.log(chalk.yellow('Cancelled.'));
            return;
        }
    }

    // Render templates
    const spinner = ora('Generating plugin skeleton...').start();
    try {
        const templateRoot = path.join(__dirname, '..', '..', 'templates', 'plugin');
        const ctx = buildContext(answers);

        await renderTemplateTree(templateRoot, targetDir, ctx, {
            includeFlyway: answers.flyway,
        });
        spinner.succeed('Plugin skeleton generated.');
    } catch (err) {
        spinner.fail('Failed to generate plugin skeleton.');
        // Cleanup partial output
        await fs.remove(targetDir).catch(() => { });
        throw err;
    }

    // Auto-register in parent pom.xml
    if (answers.register) {
        const regSpinner = ora('Registering module in parent pom.xml...').start();
        try {
            const moduleName = `plugins/${answers.name}-plugin`;
            const added = await registerInParentPom(parentPom, moduleName);
            if (added) {
                regSpinner.succeed(`Module '${moduleName}' added to pom.xml.`);
            } else {
                regSpinner.info(`Module '${moduleName}' is already registered in pom.xml.`);
            }
        } catch (err) {
            regSpinner.warn(`Failed to update pom.xml: ${err.message}. Add it manually.`);
        }
    }

    // Done — next steps
    console.log(chalk.green.bold('\n✓ Plugin created successfully!\n'));
    console.log(chalk.bold('Next steps:'));
    console.log(chalk.gray('  1.') + ` Review the generated files: ${chalk.cyan(targetDir)}`);
    console.log(chalk.gray('  2.') + ` Build: ${chalk.cyan('gasi plugin build ' + answers.name)}`);
    console.log(chalk.gray('  3.') + ` Deploy: ${chalk.cyan('gasi plugin deploy ' + answers.name)}`);
    console.log(chalk.gray('  4.') + ` Run: ${chalk.cyan('cd platform-app && mvn spring-boot:run')}\n`);
}

async function collectAnswers(opts, parentPomPath) {
    let raw;
    // Normalize --depends-on flag values; user can pass multiple times OR comma-separated.
    // Both forms collapse into a flat array by the Commander coercer.
    const flagDeps = (opts.dependsOn || []).map((s) => s.trim()).filter(Boolean);

    if (opts.yes) {
        // --yes: fill in defaults for anything missing, skip prompts
        const name = opts.name || 'sample';
        raw = {
            name: name.toLowerCase(),
            domain: (opts.domain || name).toLowerCase(),
            pluginPrefix: (opts.pluginPrefix || name).toLowerCase(),
            basePackage: opts.package || 'gasi.gps',
            version: opts.pluginVersion || '1.0.0',
            description: opts.description || `${_.upperFirst(name)} plugin`,
            dependsOn: flagDeps,
        };
    } else {
        // Interactive prompts — only ask for fields not provided via flags
        const questions = [];

        if (!opts.name) {
            questions.push({
                type: 'input',
                name: 'name',
                message: 'Plugin name (without "-plugin" suffix, example: payroll):',
                validate: validatePluginName,
                filter: (v) => v.trim().toLowerCase(),
            });
        }
        if (!opts.domain) {
            questions.push({
                type: 'input',
                name: 'domain',
                message: 'Java domain package name (example: payroll):',
                default: (a) => a.name || opts.name,
                validate: validateDomain,
                filter: (v) => v.trim().toLowerCase(),
            });
        }
        if (!opts.pluginPrefix) {
            questions.push({
                type: 'input',
                name: 'pluginPrefix',
                message: 'Plugin prefix, example auth (optional):',
                filter: (v) => (v || '').trim().toLowerCase(),
            });
        }
        if (!opts.package) {
            questions.push({
                type: 'input',
                name: 'basePackage',
                message: 'Base package:',
                default: 'gasi.gps',
                validate: validatePackage,
                filter: (v) => v.trim(),
            });
        }
        if (!opts.pluginVersion) {
            questions.push({
                type: 'input',
                name: 'version',
                message: 'Plugin version:',
                default: '1.0.0',
            });
        }
        if (!opts.description) {
            questions.push({
                type: 'input',
                name: 'description',
                message: 'Plugin description:',
                default: (a) => `${_.upperFirst(a.name || opts.name)} plugin`,
            });
        }

        const answers = await inquirer.prompt(questions);
        const interim = {
            name: opts.name || answers.name,
            domain: opts.domain || answers.domain,
            pluginPrefix: opts.pluginPrefix || answers.pluginPrefix,
            basePackage: opts.package || answers.basePackage,
            version: opts.pluginVersion || answers.version,
            description: opts.description || answers.description,
        };

        // Plugin dependency picker (interactive).
        // Flag values from --depends-on are pre-selected so the user sees them.
        const deps = await promptPluginDependencies(parentPomPath, interim.name, flagDeps);

        raw = { ...interim, dependsOn: deps };
    }

    // Validate — runs for both --yes and interactive (interactive already filtered
    // through inquirer's `validate`, but flag-provided values bypass that).
    const checks = [
        ['name', validatePluginName(raw.name)],
        ['domain', validateDomain(raw.domain)],
        ['package', validatePackage(raw.basePackage)],
    ];
    for (const [field, result] of checks) {
        if (result !== true) {
            throw new Error(`Invalid ${field}: ${result}`);
        }
    }
    for (const dep of raw.dependsOn) {
        const r = validatePluginDependency(dep);
        if (r !== true) {
            throw new Error(`Invalid --depends-on "${dep}": ${r}`);
        }
    }
    // Self-dependency guard
    const selfId = `${raw.name}-plugin`;
    for (const dep of raw.dependsOn) {
        const baseId = dep.replace(/[?@].*$/, '');
        if (baseId === selfId) {
            throw new Error(`A plugin cannot depend on itself: "${dep}"`);
        }
    }
    // Dedupe (last wins on conflict — rare edge case)
    raw.dependsOn = Array.from(new Map(raw.dependsOn.map((d) => [d.replace(/[?@].*$/, ''), d])).values());

    // Warn (not error) if a depended-on plugin isn't in the parent pom.
    if (raw.dependsOn.length && parentPomPath) {
        try {
            const knownPlugins = new Set(await listPluginModules(parentPomPath, selfId));
            const missing = raw.dependsOn
                .map((d) => d.replace(/[?@].*$/, ''))
                .filter((id) => !knownPlugins.has(id));
            if (missing.length) {
                console.log(
                    '\n' +
                    chalk.yellow('⚠ Warning: ') +
                    `the following plugins are not registered in the parent pom.xml: ${chalk.bold(missing.join(', '))}`,
                );
                console.log(chalk.gray('  PF4J will fail to load this plugin at runtime if the dependency is missing.'));
            }
        } catch (_) {
            // Non-fatal — parent pom read failure should not block generation.
        }
    }

    return {
        ...raw,
        flyway: opts.flyway !== false,
        register: opts.register !== false,
    };
}

/**
 * Interactive picker for plugin dependencies.
 *
 * Strategy:
 *   1. Scan parent pom for existing *-plugin modules and offer them as a multi-select.
 *   2. Allow the user to add additional entries manually (for plugins not yet in parent
 *      pom, or for adding version constraints / optional markers).
 *   3. Pre-select any IDs already passed via --depends-on flags.
 *
 * @returns {Promise<string[]>} final list of dependency strings in PF4J format.
 */
async function promptPluginDependencies(parentPomPath, ownPluginName, preSelectedFlags) {
    let known = [];
    try {
        known = await listPluginModules(parentPomPath, `${ownPluginName}-plugin`);
    } catch (_) {
        // best-effort — if parent pom can't be read, we just skip the picker section.
    }

    // Map flag input back to ids so we can preselect.
    const preIds = new Set(preSelectedFlags.map((d) => d.replace(/[?@].*$/, '')));

    const result = [];

    if (known.length > 0) {
        const { selected } = await inquirer.prompt([
            {
                type: 'checkbox',
                name: 'selected',
                message: 'Plugin dependencies (select any required plugins, or skip if none):',
                choices: known.map((id) => ({
                    name: id,
                    value: id,
                    checked: preIds.has(id),
                })),
            },
        ]);
        result.push(...selected);
    }

    // Add any flag-provided deps that weren't in the known list (e.g. with version constraints
    // or for plugins not yet registered) — preserve user's exact string.
    for (const dep of preSelectedFlags) {
        const baseId = dep.replace(/[?@].*$/, '');
        if (!result.includes(dep) && !result.includes(baseId)) {
            result.push(dep);
        }
    }

    // Offer the manual-add escape hatch for version constraints / optional markers /
    // plugins not yet registered in parent.
    const { addMore } = await inquirer.prompt([
        {
            type: 'confirm',
            name: 'addMore',
            message: 'Add dependencies manually (for version constraints, optional deps, or plugins not in parent pom)?',
            default: false,
        },
    ]);

    if (addMore) {
        let keepGoing = true;
        while (keepGoing) {
            const { entry } = await inquirer.prompt([
                {
                    type: 'input',
                    name: 'entry',
                    message: 'Format: <plugin-id>[@<version>][?]. Leave empty to stop.',
                    validate: (v) => {
                        const t = (v || '').trim();
                        if (!t) return true; // empty → stop
                        return validatePluginDependency(t);
                    },
                    filter: (v) => v.trim(),
                },
            ]);
            if (!entry) {
                keepGoing = false;
            } else {
                result.push(entry);
            }
        }
    }

    return result;
}

function buildContext(a) {
    const fullPackage = `${a.basePackage}.${a.domain}`;
    // PKG_PATH is base only (e.g. "gasi/gps"); domain folder appended via [[DOMAIN]] in template path
    const pkgPath = a.basePackage.replace(/\./g, '/');
    const domainClassPrefix = _.upperFirst(_.camelCase(a.name));
    const pluginClassName = `${domainClassPrefix}Plugin`;
    const extClassName = `${domainClassPrefix}AppExtension`;
    const flywayExtClassName = `${domainClassPrefix}FlywayMigrationExtension`;
    const i18nExtClassName = `${domainClassPrefix}I18nExtension`;

    return {
        PLUGIN_NAME: a.name,
        PLUGIN_PREFIX: a.pluginPrefix,
        PLUGIN_ID: `${a.name}-plugin`,
        PLUGIN_VERSION: a.version,
        PLUGIN_DESCRIPTION: a.description,
        PLUGIN_DEPENDENCIES: a.dependsOn.join(', '),
        PLUGIN_CLASS_NAME: pluginClassName,
        EXTENSION_CLASS_NAME: extClassName,
        FLYWAY_EXT_CLASS_NAME: flywayExtClassName,
        I18N_EXT_CLASS_NAME: i18nExtClassName,
        DOMAIN: a.domain,
        DOMAIN_CAP: _.upperFirst(a.domain),
        BASE_PACKAGE: a.basePackage,
        FULL_PACKAGE: fullPackage,
        PKG_PATH: pkgPath,
        FLYWAY_LOCATION: `db/migration/${a.name}`,
        I18N_BASENAME: `classpath:i18n/${a.name}/messages`,
        MIGRATION_TIMESTAMP: nowTimestamp(),
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

module.exports = pluginCreate;
