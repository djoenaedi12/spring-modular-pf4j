const fs = require('fs-extra');

/**
 * Register a new module name inside the <modules> block of a parent pom.xml.
 *
 * Why string-based, not XML-parser-based:
 *   Maven poms are human-edited. Round-tripping through xmldom/fast-xml-parser
 *   reformats whitespace, drops comments, and collapses self-closing tags —
 *   which makes diffs noisy and pisses people off. A targeted regex-based
 *   insert is good enough for this single, well-defined operation.
 *
 * @returns {Promise<boolean>} true if module was added, false if it already existed.
 */
async function registerInParentPom(pomPath, moduleName) {
    let content = await fs.readFile(pomPath, 'utf8');

    // Locate <modules>...</modules>
    const modulesMatch = content.match(/<modules>([\s\S]*?)<\/modules>/);
    if (!modulesMatch) {
        throw new Error('Could not find a <modules> block in pom.xml. Is this a multi-module parent POM?');
    }

    const modulesBlock = modulesMatch[1];

    // Idempotent: skip if already registered
    const moduleRegex = new RegExp(`<module>\\s*${escapeRegex(moduleName)}\\s*</module>`);
    if (moduleRegex.test(modulesBlock)) {
        return false;
    }

    // Match leading indentation of existing <module> entries (so insertion blends in)
    const existingEntry = modulesBlock.match(/(\s*)<module>([^<]+)<\/module>/);
    const indent = existingEntry ? existingEntry[1] : '\n        ';

    // Append before </modules> while preserving the trailing indent of the closing tag
    const newEntry = `${indent}<module>${moduleName}</module>`;
    const updatedModulesBlock = modulesBlock.replace(/(\s*)$/, `${newEntry}$1`);

    content = content.replace(
        /<modules>[\s\S]*?<\/modules>/,
        `<modules>${updatedModulesBlock}</modules>`,
    );

    await fs.writeFile(pomPath, content, 'utf8');
    return true;
}

/**
 * Remove a module name from the <modules> block of a parent pom.xml.
 *
 * @returns {Promise<boolean>} true if module was removed, false if it was not registered.
 */
async function unregisterFromParentPom(pomPath, moduleName) {
    let content = await fs.readFile(pomPath, 'utf8');

    const modulesMatch = content.match(/<modules>([\s\S]*?)<\/modules>/);
    if (!modulesMatch) {
        throw new Error('Could not find a <modules> block in pom.xml. Is this a multi-module parent POM?');
    }

    const modulesBlock = modulesMatch[1];
    const moduleLineRegex = new RegExp(`\\n[ \\t]*<module>\\s*${escapeRegex(moduleName)}\\s*</module>`, 'm');
    if (!moduleLineRegex.test(modulesBlock)) {
        return false;
    }

    const updatedModulesBlock = modulesBlock.replace(moduleLineRegex, '');
    content = content.replace(
        /<modules>[\s\S]*?<\/modules>/,
        `<modules>${updatedModulesBlock}</modules>`,
    );

    await fs.writeFile(pomPath, content, 'utf8');
    return true;
}

function escapeRegex(s) {
    return s.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
}

module.exports = { registerInParentPom, unregisterFromParentPom };
