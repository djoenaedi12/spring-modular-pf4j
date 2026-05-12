const chalk = require('chalk');

const { getPluginModules, resolveCwd } = require('../plugin-utils');

async function pluginList(opts) {
    const cwd = resolveCwd(opts);
    const modules = await getPluginModules(cwd);

    if (!modules.length) {
        console.log(chalk.yellow('No plugin modules are registered in the parent pom.xml.'));
        return;
    }

    console.log(chalk.bold('Registered plugins:'));
    for (const moduleName of modules) {
        console.log(`  ${moduleName}`);
    }
}

module.exports = pluginList;
