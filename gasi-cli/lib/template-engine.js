const path = require('path');
const fs = require('fs-extra');

/**
 * Walk a template directory, copy every file to the target directory,
 * replacing placeholder tokens in both:
 *   - file/directory names:  [[TOKEN]]   (square brackets — won't appear in real paths)
 *   - file contents:         {{TOKEN}}   (visible, doesn't clash with Java syntax)
 *
 * Path placeholders use [[...]] because __TOKEN__ collapses ambiguously when two
 * tokens sit next to each other in a filename (e.g. `V__TS____init.sql`).
 * Brackets are unambiguous and illegal in Java identifiers and Maven artifacts.
 */
async function renderTemplateTree(templateRoot, targetRoot, ctx, opts = {}) {
    const entries = await walk(templateRoot);

    for (const entry of entries) {
        const relPath = path.relative(templateRoot, entry.fullPath);

        // Skip the whole Flyway migration tree when user opts out.
        if (!opts.includeFlyway && relPath.includes(path.join('db', 'migration'))) {
            continue;
        }

        if (typeof opts.shouldInclude === 'function' && !opts.shouldInclude(relPath, entry)) {
            continue;
        }

        const renderedRelPath = replacePathTokens(relPath, ctx);
        const targetPath = path.join(targetRoot, renderedRelPath);

        if (entry.isDirectory) {
            await fs.ensureDir(targetPath);
        } else {
            await fs.ensureDir(path.dirname(targetPath));
            const content = await fs.readFile(entry.fullPath, 'utf8');
            const rendered = normalizeRenderedContent(replaceContentTokens(content, ctx), renderedRelPath);
            await fs.writeFile(targetPath, rendered, 'utf8');
        }
    }
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

function replacePathTokens(p, ctx) {
    return p.replace(/\[\[([A-Z0-9_]+)\]\]/g, (match, key) => (key in ctx ? ctx[key] : match));
}

function replaceContentTokens(content, ctx) {
    return content.replace(/\{\{([A-Z0-9_]+)\}\}/g, (match, key) => (key in ctx ? ctx[key] : match));
}

function normalizeRenderedContent(content, relPath = '') {
    if (!relPath.endsWith('.java')) {
        return content;
    }

    let normalized = content.replace(/[ \t]+$/gm, '');

    // Keep generated import blocks compact even when optional import tokens are empty.
    normalized = normalized.replace(/(import [^;]+;\n)\n+(import )/g, '$1$2');

    // Keep annotations directly attached to the method they annotate.
    normalized = normalized.replace(/(    @[^\n]+)\n\n+(    @(Override|Mapping))/g, '$1\n$2');

    // Collapse excessive empty vertical space introduced by empty template tokens.
    normalized = normalized.replace(/\n{3,}/g, '\n\n');

    return normalized.trimEnd() + '\n';
}

module.exports = { renderTemplateTree, normalizeRenderedContent };
