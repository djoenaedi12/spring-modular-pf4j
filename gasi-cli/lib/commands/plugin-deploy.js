const path = require('path');
const fs = require('fs-extra');
const chalk = require('chalk');

const {
    assertPluginModuleExists,
    findDeployedPluginJars,
    findPluginJar,
    resolveCwd,
    resolvePluginsDir,
} = require('../plugin-utils');

async function pluginDeploy(pluginName, opts) {
    const cwd = resolveCwd(opts);
    const { pluginId, moduleDir, moduleName } = await assertPluginModuleExists(cwd, pluginName);
    const pluginsDir = resolvePluginsDir(cwd, opts);
    const sourceJar = await findPluginJar(moduleDir, pluginId);
    const targetJar = path.join(pluginsDir, path.basename(sourceJar));

    if (opts.dryRun) {
        console.log(chalk.cyan(`ensure directory ${pluginsDir}`));
        console.log(chalk.cyan(`copy ${sourceJar} -> ${targetJar}`));
        return;
    }

    await fs.ensureDir(pluginsDir);

    if (!opts.keepOld) {
        const oldJars = await findDeployedPluginJars(pluginsDir, moduleName);
        for (const oldJar of oldJars) {
            if (oldJar !== targetJar) {
                await fs.remove(oldJar);
            }
        }
    }

    await fs.copy(sourceJar, targetJar, { overwrite: true });
    console.log(chalk.green(`✓ Deployed ${path.basename(targetJar)} to ${pluginsDir}.`));
}

module.exports = pluginDeploy;
