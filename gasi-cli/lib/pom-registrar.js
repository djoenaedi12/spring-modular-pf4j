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

/**
 * Builds a Maven dependency XML block from the given dependency descriptor.
 *
 * Optional fields such as version and scope are only added when provided.
 *
 * @param {Object} dependency Maven dependency descriptor.
 * @param {string} dependency.groupId Dependency group ID.
 * @param {string} dependency.artifactId Dependency artifact ID.
 * @param {string} [dependency.version] Optional dependency version.
 * @param {string} [dependency.scope] Optional dependency scope.
 * @returns {string} Maven dependency XML block.
 */
function buildDependencyXml(dependency) {
    return [
        '        <dependency>',
        `            <groupId>${dependency.groupId}</groupId>`,
        `            <artifactId>${dependency.artifactId}</artifactId>`,
        dependency.version ? `            <version>${dependency.version}</version>` : null,
        dependency.scope ? `            <scope>${dependency.scope}</scope>` : null,
        '        </dependency>',
    ].filter(Boolean).join('\n');
}

/**
 * Checks whether a pom.xml content already contains the given dependency.
 *
 * The check uses groupId and artifactId only to prevent duplicate dependency
 * entries, regardless of version or scope differences.
 *
 * @param {string} pomContent Current pom.xml content.
 * @param {Object} dependency Maven dependency descriptor.
 * @param {string} dependency.groupId Dependency group ID.
 * @param {string} dependency.artifactId Dependency artifact ID.
 * @returns {boolean} true if the dependency already exists.
 */
function hasDependency(pomContent, dependency) {
    const dependencyRegex = new RegExp(
        `<dependency>\\s*` +
        `<groupId>${escapeRegex(dependency.groupId)}</groupId>\\s*` +
        `<artifactId>${escapeRegex(dependency.artifactId)}</artifactId>` +
        `[\\s\\S]*?` +
        `</dependency>`,
        'm'
    );

    return dependencyRegex.test(pomContent);
}

/**
 * Ensures that the given Maven dependencies exist in the target pom.xml.
 *
 * Missing dependencies are appended to the existing <dependencies> section.
 * If the pom.xml does not have a <dependencies> section yet, a new one is
 * created before the closing </project> tag.
 *
 * Existing dependencies are detected by groupId and artifactId to avoid
 * duplicate entries.
 *
 * @param {string} pomPath Absolute path to the target pom.xml file.
 * @param {Array<Object>} dependencies Dependencies that must exist.
 * @param {string} dependencies[].groupId Dependency group ID.
 * @param {string} dependencies[].artifactId Dependency artifact ID.
 * @param {string} [dependencies[].version] Optional dependency version.
 * @param {string} [dependencies[].scope] Optional dependency scope.
 * @returns {Promise<boolean>} true if pom.xml was updated, false if no change was needed.
 */
async function ensurePomDependencies(pomPath, dependencies) {
    if (!(await fs.pathExists(pomPath))) {
        throw new Error(`pom.xml not found: ${pomPath}`);
    }

    let pomContent = await fs.readFile(pomPath, 'utf8');

    const missingDependencies = dependencies.filter((dependency) => {
        return !hasDependency(pomContent, dependency);
    });

    if (missingDependencies.length === 0) {
        return false;
    }

    const dependencyXml = missingDependencies
        .map(buildDependencyXml)
        .join('\n');

    if (pomContent.includes('</dependencies>')) {
        pomContent = pomContent.replace(
            '</dependencies>',
            `${dependencyXml}\n    </dependencies>`
        );
    } else {
        pomContent = pomContent.replace(
            '</project>',
            `    <dependencies>\n${dependencyXml}\n    </dependencies>\n</project>`
        );
    }

    await fs.writeFile(pomPath, pomContent, 'utf8');
    return true;
}

module.exports = { registerInParentPom, unregisterFromParentPom, ensurePomDependencies };
