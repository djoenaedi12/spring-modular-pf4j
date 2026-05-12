const fs = require('fs-extra');

/**
 * Scan the parent pom for module names ending in "-plugin".
 * Used to populate the interactive dependency picker.
 *
 * Note: this is best-effort — it doesn't actually verify each module folder
 * exists on disk, since the user might be generating a plugin that depends on
 * one not yet built.
 *
 * @returns {Promise<string[]>} list of plugin module names, excluding `excludeName` if given.
 */
async function listPluginModules(pomPath, excludeName = null) {
    const content = await fs.readFile(pomPath, 'utf8');
    const modulesMatch = content.match(/<modules>([\s\S]*?)<\/modules>/);
    if (!modulesMatch) return [];

    const out = [];
    const re = /<module>\s*([^<]+?)\s*<\/module>/g;
    let m;
    while ((m = re.exec(modulesMatch[1])) !== null) {
        const name = m[1].trim();
        if (name.endsWith('-plugin') && name !== excludeName) {
            out.push(name);
        }
    }
    return out;
}

module.exports = { listPluginModules };
