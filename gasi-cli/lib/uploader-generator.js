const path = require('path');
const fs = require('fs-extra');
const _ = require('lodash');

const { renderTemplateTree } = require('./template-engine');

async function resolvePluginPackage(pluginDir) {
    const propsFile = path.join(pluginDir, 'src', 'main', 'resources', 'plugin.properties');
    if (await fs.pathExists(propsFile)) {
        const content = await fs.readFile(propsFile, 'utf8');
        const match = content.match(/^plugin\.class[ \t]*=[ \t]*([^\r\n]*)/m);
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
        'Ensure plugin.properties contains a valid plugin.class entry.',
    );
}

function extractDomain(packageName) {
    const parts = packageName.split('.');
    return parts[parts.length - 1];
}

async function generateUploader({
    pluginDir,
    entityName,
    resourceName,
}) {
    const packageName = await resolvePluginPackage(pluginDir);
    const domain = extractDomain(packageName);
    const basePackage = packageName.substring(0, packageName.lastIndexOf('.'));
    const ctx = {
        PACKAGE_NAME: packageName,
        PKG_PATH: basePackage.split('.').join(path.sep),
        DOMAIN: domain,
        ENTITY_NAME: entityName,
        RESOURCE_NAME: resourceName || _.kebabCase(entityName),
    };

    const templateRoot = path.join(__dirname, '..', 'templates', 'uploader');
    const targetRoot = pluginDir;

    await checkConflicts(templateRoot, targetRoot, ctx);
    await renderTemplateTree(templateRoot, targetRoot, ctx);
    return collectGeneratedPaths(templateRoot, targetRoot, ctx);
}

async function checkConflicts(templateRoot, targetRoot, ctx) {
    const entries = await walk(templateRoot);
    for (const entry of entries) {
        if (entry.isDirectory) continue;

        const relPath = path.relative(templateRoot, entry.fullPath);
        const targetPath = path.join(targetRoot, replacePathTokens(relPath, ctx));
        if (await fs.pathExists(targetPath)) {
            throw new Error(`File already exists: ${targetPath}. Aborting to avoid overwriting.`);
        }
    }
}

async function collectGeneratedPaths(templateRoot, targetRoot, ctx) {
    const entries = await walk(templateRoot);
    const paths = [];
    for (const entry of entries) {
        if (entry.isDirectory) continue;

        const relPath = path.relative(templateRoot, entry.fullPath);
        paths.push(path.join(targetRoot, replacePathTokens(relPath, ctx)));
    }
    return paths;
}

function replacePathTokens(value, ctx) {
    return value.replace(/\[\[([A-Z0-9_]+)\]\]/g, (match, key) => (key in ctx ? ctx[key] : match));
}

async function walk(dir) {
    const out = [];
    async function recurse(current) {
        const items = await fs.readdir(current, { withFileTypes: true });
        for (const item of items) {
            const full = path.join(current, item.name);
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

module.exports = { generateUploader, resolvePluginPackage };
