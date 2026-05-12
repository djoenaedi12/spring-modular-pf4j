const fs = require('fs-extra');
const chalk = require('chalk');
const inquirer = require('inquirer');

const { unregisterFromParentPom } = require('../pom-registrar');
const {
    assertPluginModuleExists,
    assertProjectRoot,
    findDeployedPluginJars,
    resolveCwd,
    resolvePluginsDir,
} = require('../plugin-utils');

async function pluginDelete(pluginName, opts) {
    const cwd = resolveCwd(opts);
    const parentPom = await assertProjectRoot(cwd);
    const { pluginId, moduleName, moduleDir } = await assertPluginModuleExists(cwd, pluginName);
    const pluginsDir = resolvePluginsDir(cwd, opts);
    const deployedJars = opts.keepDeployed
        ? []
        : await findDeployedPluginJars(pluginsDir, pluginId);

    if (opts.dryRun) {
        printPlan(moduleName, moduleDir, parentPom, deployedJars);
        return;
    }

    if (!opts.yes) {
        printPlan(moduleName, moduleDir, parentPom, deployedJars);
        const { confirm } = await inquirer.prompt([
            {
                type: 'confirm',
                name: 'confirm',
                message: `Delete ${moduleName}? This removes source files and unregisters the module.`,
                default: false,
            },
        ]);

        if (!confirm) {
            console.log(chalk.yellow('Cancelled.'));
            return;
        }
    }

    await unregisterFromParentPom(parentPom, moduleName);
    await fs.remove(moduleDir);

    for (const jar of deployedJars) {
        await fs.remove(jar);
    }

    console.log(chalk.green(`✓ Deleted ${moduleName}.`));
}

function printPlan(moduleName, moduleDir, parentPom, deployedJars) {
    console.log(chalk.bold(`Delete plan for ${moduleName}:`));
    console.log(`  Unregister from: ${parentPom}`);
    console.log(`  Remove directory: ${moduleDir}`);
    if (deployedJars.length) {
        console.log('  Remove deployed JARs:');
        for (const jar of deployedJars) {
            console.log(`    ${jar}`);
        }
    } else {
        console.log('  Remove deployed JARs: none');
    }
}

module.exports = pluginDelete;
