const path = require('path');
const fs = require('fs-extra');
const chalk = require('chalk');
const ora = require('ora');
const inquirer = require('inquirer');
const _ = require('lodash');
const pluralize = require('pluralize');

const { resolveCwd, assertProjectRoot, getPluginModules, detectPluginFromCwd } = require('../plugin-utils');
const { validateEntityName } = require('../validators');

async function resolvePluginPackage(pluginDir) {
    const propsFile = path.join(pluginDir, 'src', 'main', 'resources', 'plugin.properties');
    if (await fs.pathExists(propsFile)) {
        const content = await fs.readFile(propsFile, 'utf8');
        const match = content.match(/^plugin\.class\s*=\s*(.+)$/m);
        if (match) {
            const fqcn = match[1].trim();
            const lastDot = fqcn.lastIndexOf('.');
            if (lastDot > 0) return fqcn.substring(0, lastDot);
        }
    }
    return null;
}

function resourceFiles(entityName) {
    return [
        `domain/model/${entityName}.java`,
        `domain/port/inbound/${entityName}Service.java`,
        `domain/port/outbound/${entityName}RepositoryPort.java`,
        `application/dto/${entityName}CreateRequest.java`,
        `application/dto/${entityName}UpdateRequest.java`,
        `application/dto/${entityName}SummaryResponse.java`,
        `application/dto/${entityName}DetailResponse.java`,
        `application/mapper/${entityName}DtoMapper.java`,
        `application/service/${entityName}ServiceImpl.java`,
        `infrastructure/entity/${entityName}Entity.java`,
        `infrastructure/mapper/${entityName}Mapper.java`,
        `infrastructure/adapter/${entityName}RepositoryAdapter.java`,
        `infrastructure/persistence/${entityName}EntityRepository.java`,
        `presentation/controller/${entityName}Controller.java`,
    ];
}

async function resourceDelete(entityName, opts) {
    console.log(chalk.cyan.bold('\n  gasi — Resource Delete\n'));

    // Validate entity name
    const result = validateEntityName(entityName);
    if (result !== true) throw new Error(result);

    // ── Detect or select plugin ──────────────────────────────────────
    let pluginModule, pluginDir, pkg, srcRoot, projectRoot;

    const detected = await detectPluginFromCwd(resolveCwd(opts));
    if (detected) {
        projectRoot = detected.projectRoot;
        pluginModule = detected.pluginModule;
        pluginDir = detected.pluginDir;
        pkg = await resolvePluginPackage(pluginDir);
        if (!pkg) throw new Error(`Could not determine package for ${pluginModule}.`);
        srcRoot = path.join(pluginDir, 'src', 'main', 'java', pkg.replace(/\./g, '/'));
        console.log(`  Plugin: ${chalk.green(pluginModule)}\n`);
    } else {
        projectRoot = resolveCwd(opts);
        await assertProjectRoot(projectRoot);

        const pluginModules = await getPluginModules(projectRoot);
        if (!pluginModules.length) {
            throw new Error('No plugin modules found in the parent pom.xml.');
        }

        const pluginPackages = new Map();
        for (const mod of pluginModules) {
            const modDir = path.join(projectRoot, mod);
            const modPkg = await resolvePluginPackage(modDir);
            if (modPkg) pluginPackages.set(mod, modPkg);
        }

        const pluginChoices = [...pluginPackages.keys()];
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
        pluginDir = path.join(projectRoot, pluginModule);
        pkg = pluginPackages.get(pluginModule);
        srcRoot = path.join(pluginDir, 'src', 'main', 'java', pkg.replace(/\./g, '/'));
        console.log(`  Plugin: ${chalk.green(pluginModule)}\n`);
    }

    const pluginName = path.basename(pluginModule).replace(/-plugin$/, '');
    const migrationDir = path.join(pluginDir, 'src', 'main', 'resources', 'db', 'migration', pluginName);

    const files = resourceFiles(entityName);
    const existing = [];
    const missing = [];

    for (const rel of files) {
        const abs = path.join(srcRoot, rel);
        if (await fs.pathExists(abs)) {
            existing.push(abs);
        } else {
            missing.push(abs);
        }
    }

    // Find migration files if --include-migration
    if (opts.includeMigration) {
        const tableName = pluralize(_.snakeCase(entityName));
        if (await fs.pathExists(migrationDir)) {
            const entries = await fs.readdir(migrationDir);
            const migrationFiles = entries
                .filter((e) => e.includes(`__create_${tableName}.sql`))
                .map((e) => path.join(migrationDir, e));
            existing.push(...migrationFiles);
        }
    }

    if (!existing.length) {
        throw new Error(`No resource files found for entity "${entityName}" in ${pluginModule}.`);
    }

    console.log(chalk.bold('Files to delete:'));
    for (const f of existing) {
        console.log(chalk.red('    ' + path.relative(projectRoot, f)));
    }
    if (missing.length) {
        console.log(chalk.gray(`\n  ${missing.length} file(s) not found (already deleted or never generated).`));
    }
    if (!opts.includeMigration) {
        console.log(chalk.yellow('\n  Note: Migration SQL files are not deleted. Use --include-migration to delete them.'));
    }

    if (!opts.yes) {
        const { confirm } = await inquirer.prompt([
            { type: 'confirm', name: 'confirm', message: 'Delete these files?', default: false },
        ]);
        if (!confirm) {
            console.log(chalk.yellow('Cancelled.'));
            return;
        }
    }

    const spinner = ora('Deleting resource files...').start();
    try {
        for (const f of existing) {
            await fs.remove(f);
        }

        // Clean up empty directories
        const dirsToCheck = [
            ...new Set(existing.map((f) => path.dirname(f))),
        ].sort((a, b) => b.length - a.length);

        for (const dir of dirsToCheck) {
            let current = dir;
            while (current.startsWith(srcRoot) || current.startsWith(migrationDir)) {
                if (current === srcRoot || current === path.dirname(migrationDir)) break;
                try {
                    const entries = await fs.readdir(current);
                    if (entries.length === 0) {
                        await fs.remove(current);
                        current = path.dirname(current);
                    } else {
                        break;
                    }
                } catch (_) {
                    break;
                }
            }
        }

        spinner.succeed(`Deleted ${existing.length} files.`);
    } catch (err) {
        spinner.fail('Failed to delete resource files.');
        throw err;
    }

    console.log(chalk.green.bold('\n✓ Resource deleted successfully!\n'));
}

module.exports = resourceDelete;
