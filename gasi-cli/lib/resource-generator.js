const path = require('path');
const fs = require('fs-extra');

const { renderTemplateTree } = require('./template-engine');
const { buildResourceContext } = require('./resource-templates');

/**
 * Resolve the full Java package for a plugin by reading plugin.properties.
 */
async function resolvePluginPackage(pluginDir) {
    const propsFile = path.join(pluginDir, 'src', 'main', 'resources', 'plugin.properties');
    if (await fs.pathExists(propsFile)) {
        const content = await fs.readFile(propsFile, 'utf8');
        const match = content.match(/^plugin\.class\s*=\s*(.+)$/m);
        if (match) {
            const fqcn = match[1].trim();
            const lastDot = fqcn.lastIndexOf('.');
            if (lastDot > 0) {
                return fqcn.substring(0, lastDot);
            }
        }
    }

    throw new Error(
        `Could not determine the Java package for plugin at ${pluginDir}. ` +
        `Ensure plugin.properties contains a valid plugin.class entry.`,
    );
}

/**
 * Extract the domain segment from the full package.
 * e.g. "gasi.gps.payroll" → "payroll"
 */
function extractDomain(pkg) {
    const parts = pkg.split('.');
    return parts[parts.length - 1];
}

/**
 * Generate all resource files for a single entity in the target plugin.
 *
 * Uses the template files in templates/resource/ with token replacement.
 *
 * @returns {string[]} list of absolute paths of generated files.
 */
async function generateResource({ cwd, pluginDir, pluginName, pluginPrefix, entityName, tableName, fields }) {
    const pkg = await resolvePluginPackage(pluginDir);
    const domain = extractDomain(pkg);

    // Build the context with all computed token values
    const ctx = buildResourceContext({
        pkg,
        entityName,
        tableName,
        fields,
        pluginName,
        pluginPrefix,
        domain,
    });

    // Collect files before rendering to check for conflicts
    const templateRoot = path.join(__dirname, '..', 'templates', 'resource');
    const targetRoot = path.join(pluginDir);

    // Check for existing files first
    await checkConflicts(templateRoot, targetRoot, ctx);

    // Render all templates into the plugin directory
    await renderTemplateTree(templateRoot, targetRoot, ctx, { includeFlyway: true });

    // Collect generated file paths for reporting
    const generated = await collectGeneratedPaths(templateRoot, targetRoot, ctx);

    return generated;
}

/**
 * Walk the template tree and check if any target file already exists.
 * Throws on conflict to avoid accidental overwrites.
 */
async function checkConflicts(templateRoot, targetRoot, ctx) {
    const entries = await walk(templateRoot);

    for (const entry of entries) {
        if (entry.isDirectory) continue;

        const relPath = path.relative(templateRoot, entry.fullPath);
        const renderedRelPath = replacePathTokens(relPath, ctx);
        const targetPath = path.join(targetRoot, renderedRelPath);

        if (await fs.pathExists(targetPath)) {
            throw new Error(`File already exists: ${targetPath}. Aborting to avoid overwriting.`);
        }
    }
}

/**
 * Collect the absolute paths of all files that were generated.
 */
async function collectGeneratedPaths(templateRoot, targetRoot, ctx) {
    const entries = await walk(templateRoot);
    const paths = [];

    for (const entry of entries) {
        if (entry.isDirectory) continue;

        const relPath = path.relative(templateRoot, entry.fullPath);
        const renderedRelPath = replacePathTokens(relPath, ctx);
        const targetPath = path.join(targetRoot, renderedRelPath);
        paths.push(targetPath);
    }

    return paths;
}

// Re-use the same token replacement logic as template-engine.js
function replacePathTokens(p, ctx) {
    return p.replace(/\[\[([A-Z0-9_]+)\]\]/g, (match, key) => (key in ctx ? ctx[key] : match));
}

async function walk(dir) {
    const out = [];
    async function recurse(d) {
        const items = await fs.readdir(d, { withFileTypes: true });
        for (const item of items) {
            const full = path.join(d, item.name);
            if (item.isDirectory()) {
                out.push({ fullPath: full, isDirectory: true, isFile: false });
                await recurse(full);
            } else if (item.isFile()) {
                out.push({ fullPath: full, isDirectory: false, isFile: true });
            }
        }
    }
    await recurse(dir);
    return out;
}

module.exports = { generateResource };
