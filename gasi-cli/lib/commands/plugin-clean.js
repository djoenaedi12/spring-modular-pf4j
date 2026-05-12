const fs = require('fs-extra');
const chalk = require('chalk');

const {
    assertPluginModuleExists,
    findDeployedPluginJars,
    resolveCwd,
    resolvePluginsDir,
} = require('../plugin-utils');

async function pluginClean(pluginName, opts) {
    const cwd = resolveCwd(opts);
    const { pluginId, moduleName, moduleDir } = await assertPluginModuleExists(cwd, pluginName);
    const pluginsDir = resolvePluginsDir(cwd, opts);
    const deployedJars = await findDeployedPluginJars(pluginsDir, pluginId);

    if (!deployedJars.length) {
        console.log(chalk.yellow(`No deployed JARs found for ${pluginId} in ${pluginsDir}.`));
        return;
    }

    if (opts.dryRun) {
        for (const jar of deployedJars) {
            console.log(chalk.cyan(`remove ${jar}`));
        }
        return;
    }

    for (const jar of deployedJars) {
        await fs.remove(jar);
    }

    console.log(chalk.green(`✓ Removed ${deployedJars.length} deployed JAR(s) for ${pluginId}.`));
}

module.exports = pluginClean;
