const { spawn } = require('child_process');
const chalk = require('chalk');

const { assertPluginModuleExists, resolveCwd } = require('../plugin-utils');

async function pluginBuild(pluginName, opts) {
    const cwd = resolveCwd(opts);
    const { pluginId, moduleName, moduleDir } = await assertPluginModuleExists(cwd, pluginName);
    const args = [];

    if (!opts.verbose) {
        args.push('-q');
    }

    args.push('clean', 'package', '-pl', moduleName, '-am');

    if (opts.skipTests) {
        args.push('-DskipTests');
    }
    if (opts.profile) {
        args.push(`-P${opts.profile}`);
    }

    if (opts.dryRun) {
        console.log(chalk.cyan(`mvn ${args.join(' ')}`));
        return;
    }

    console.log(chalk.bold(`Building ${moduleName}...`));
    await runMaven(args, cwd, opts.verbose);
    console.log(chalk.green(`✓ Build completed for ${moduleName}.`));
}

function runMaven(args, cwd, verbose) {
    return new Promise((resolve, reject) => {
        const child = spawn('mvn', args, {
            cwd,
            shell: process.platform === 'win32',
            stdio: verbose ? 'inherit' : ['ignore', 'inherit', 'inherit'],
        });

        child.on('error', reject);
        child.on('close', (code) => {
            if (code === 0) {
                resolve();
            } else {
                reject(new Error(`Maven exited with code ${code}.`));
            }
        });
    });
}

module.exports = pluginBuild;
