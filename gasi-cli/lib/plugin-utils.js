const path = require('path');
const fs = require('fs-extra');

const { listPluginModules } = require('./pom-scanner');

function resolveCwd(opts = {}) {
    return opts.cwd ? path.resolve(opts.cwd) : process.cwd();
}

async function assertProjectRoot(cwd) {
    const parentPom = path.join(cwd, 'pom.xml');
    if (!(await fs.pathExists(parentPom))) {
        throw new Error(`No pom.xml found in ${cwd}. Run this from the project root or use --cwd.`);
    }
    return parentPom;
}

function normalizePluginModuleName(name) {
    const raw = (name || '').trim();
    if (!raw) {
        throw new Error('Plugin name is required.');
    }
    return raw.endsWith('-plugin') ? raw : `${raw}-plugin`;
}

async function getPluginModules(cwd) {
    const parentPom = await assertProjectRoot(cwd);
    return listPluginModules(parentPom);
}

async function assertPluginModuleExists(cwd, pluginName) {
    const pluginId = normalizePluginModuleName(pluginName);
    const moduleName = `plugins/${pluginId}`;
    const modules = await getPluginModules(cwd);
    if (!modules.includes(moduleName)) {
        throw new Error(`Plugin module '${moduleName}' is not registered in the parent pom.xml.`);
    }

    const moduleDir = path.join(cwd, moduleName);
    if (!(await fs.pathExists(moduleDir))) {
        throw new Error(`Plugin module directory does not exist: ${moduleDir}`);
    }

    return { pluginId, moduleName, moduleDir };
}

function resolvePluginsDir(cwd, opts = {}) {
    return path.resolve(cwd, opts.pluginsDir || path.join('platform-app', 'plugins'));
}

async function findPluginJar(moduleDir, moduleName) {
    const targetDir = path.join(moduleDir, 'target');
    if (!(await fs.pathExists(targetDir))) {
        throw new Error(`Target directory does not exist: ${targetDir}. Build the plugin first.`);
    }

    const entries = await fs.readdir(targetDir);
    const candidates = [];
    for (const entry of entries) {
        if (!entry.endsWith('.jar')) continue;
        if (!entry.startsWith(`${moduleName}-`)) continue;
        if (entry.endsWith('-sources.jar')) continue;
        if (entry.endsWith('-javadoc.jar')) continue;
        if (entry.startsWith('original-')) continue;

        const fullPath = path.join(targetDir, entry);
        const stat = await fs.stat(fullPath);
        if (stat.isFile()) {
            candidates.push({ path: fullPath, mtimeMs: stat.mtimeMs });
        }
    }

    if (!candidates.length) {
        throw new Error(`No deployable JAR found in ${targetDir}. Build the plugin first.`);
    }

    candidates.sort((a, b) => b.mtimeMs - a.mtimeMs);
    return candidates[0].path;
}

async function findDeployedPluginJars(pluginsDir, moduleName) {
    if (!(await fs.pathExists(pluginsDir))) {
        return [];
    }

    const entries = await fs.readdir(pluginsDir);
    return entries
        .filter((entry) => entry.endsWith('.jar'))
        .filter((entry) => entry.startsWith(`${moduleName}-`))
        .map((entry) => path.join(pluginsDir, entry));
}

/**
 * Detect if cwd is inside a plugin directory.
 * Walks up from cwd looking for a *-plugin folder pattern.
 *
 * @returns {{ pluginDir, pluginModule, projectRoot } | null}
 */
async function detectPluginFromCwd(cwd) {
    let current = path.resolve(cwd);

    while (current !== path.dirname(current)) {
        const dirName = path.basename(current);

        if (dirName.endsWith('-plugin')) {
            const parentDir = path.dirname(current);
            if (path.basename(parentDir) === 'plugins') {
                const projectRoot = path.dirname(parentDir);
                const parentPom = path.join(projectRoot, 'pom.xml');
                if (await fs.pathExists(parentPom)) {
                    return {
                        pluginDir: current,
                        pluginModule: `plugins/${dirName}`,
                        projectRoot,
                    };
                }
            }
        }

        current = path.dirname(current);
    }

    return null;
}

module.exports = {
    assertPluginModuleExists,
    assertProjectRoot,
    detectPluginFromCwd,
    findDeployedPluginJars,
    findPluginJar,
    getPluginModules,
    normalizePluginModuleName,
    resolveCwd,
    resolvePluginsDir,
};
