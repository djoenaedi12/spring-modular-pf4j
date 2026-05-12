function validatePluginName(value) {
    const v = (value || '').trim();
    if (!v) return 'Plugin name is required.';
    if (!/^[a-z][a-z0-9-]*$/.test(v)) {
        return 'Use lowercase letters, numbers, and dashes only. Must start with a letter. Example: payroll, time-attendance.';
    }
    if (v.endsWith('-plugin')) {
        return 'Do not include the "-plugin" suffix; it will be added automatically.';
    }
    return true;
}

function validateDomain(value) {
    const v = (value || '').trim();
    if (!v) return 'Domain is required.';
    if (!/^[a-z][a-z0-9]*$/.test(v)) {
        return 'Use lowercase letters and numbers only, without dashes. This becomes the Java package folder. Example: payroll, timeattendance.';
    }
    if (isJavaKeyword(v)) {
        return `"${v}" is a Java keyword and cannot be used as a package name.`;
    }
    return true;
}

function validatePackage(value) {
    const v = (value || '').trim();
    if (!v) return 'Package is required.';
    // valid Java package: segments of [a-zA-Z_$][a-zA-Z0-9_$]* separated by dots
    if (!/^[a-zA-Z_$][a-zA-Z0-9_$]*(\.[a-zA-Z_$][a-zA-Z0-9_$]*)*$/.test(v)) {
        return 'Invalid package format. Example: gasi.gps, com.example.app.';
    }
    for (const seg of v.split('.')) {
        if (isJavaKeyword(seg)) {
            return `Segment "${seg}" is a Java keyword.`;
        }
    }
    return true;
}

const JAVA_KEYWORDS = new Set([
    'abstract', 'assert', 'boolean', 'break', 'byte', 'case', 'catch', 'char',
    'class', 'const', 'continue', 'default', 'do', 'double', 'else', 'enum',
    'extends', 'final', 'finally', 'float', 'for', 'goto', 'if', 'implements',
    'import', 'instanceof', 'int', 'interface', 'long', 'native', 'new',
    'package', 'private', 'protected', 'public', 'return', 'short', 'static',
    'strictfp', 'super', 'switch', 'synchronized', 'this', 'throw', 'throws',
    'transient', 'try', 'void', 'volatile', 'while', 'true', 'false', 'null',
    'var', 'yield', 'record', 'sealed', 'non-sealed', 'permits',
]);

function isJavaKeyword(s) {
    return JAVA_KEYWORDS.has(s);
}

/**
 * Validate a single PF4J plugin dependency string.
 * Format: <plugin-id>[@<version-constraint>][?]
 *
 *   auth-plugin               → required, any version
 *   auth-plugin@1.0.0         → required, exact version
 *   auth-plugin@>=1.0.0       → required, version constraint
 *   auth-plugin?              → optional, any version
 *   auth-plugin@>=1.0.0?      → optional, version constraint
 *
 * Reference: https://pf4j.org/doc/plugins.html (Plugin-Dependencies header)
 */
function validatePluginDependency(value) {
    const v = (value || '').trim();
    if (!v) return 'Dependency string is empty.';

    // Strip optional marker
    const optional = v.endsWith('?');
    const core = optional ? v.slice(0, -1) : v;

    // Split id and version constraint
    const atIdx = core.indexOf('@');
    const id = atIdx === -1 ? core : core.slice(0, atIdx);
    const version = atIdx === -1 ? null : core.slice(atIdx + 1);

    if (!/^[a-z][a-z0-9-]*$/.test(id)) {
        return `Plugin id "${id}" is invalid. Use lowercase letters, numbers, and dashes only; it must start with a letter.`;
    }

    if (version !== null) {
        if (!version) {
            return `Version constraint is empty after "@" in "${v}".`;
        }
        // Accept: 1.0.0, >=1.0.0, <2.0.0, ~1.0, ^1.0, *, etc.
        // Just check that it has some valid characters; PF4J uses jSemVer internally.
        if (!/^[0-9*xX.\-+\s<>=~^]+$/.test(version)) {
            return `Version constraint "${version}" contains invalid characters.`;
        }
    }

    return true;
}

function validateEntityName(value) {
    const v = (value || '').trim();
    if (!v) return 'Entity name is required.';
    if (!/^[A-Z][a-zA-Z0-9]*$/.test(v)) {
        return 'Must be PascalCase starting with an uppercase letter. Example: Employee, PayrollItem.';
    }
    if (isJavaKeyword(v.toLowerCase())) {
        return `"${v}" conflicts with a Java keyword.`;
    }
    return true;
}

function validateFieldName(value) {
    const v = (value || '').trim();
    if (!v) return 'Field name is required.';
    if (!/^[a-z][a-zA-Z0-9]*$/.test(v)) {
        return 'Must be camelCase starting with a lowercase letter. Example: fullName, departmentId.';
    }
    if (isJavaKeyword(v)) {
        return `"${v}" is a Java keyword.`;
    }
    return true;
}

function validateEnumName(value) {
    const v = (value || '').trim();
    if (!v) return 'Enum name is required.';
    if (!/^[A-Z][a-zA-Z0-9]*$/.test(v)) {
        return 'Must be PascalCase starting with an uppercase letter. Example: EmployeeStatus, Gender.';
    }
    return true;
}

module.exports = { validatePluginName, validateDomain, validatePackage, validatePluginDependency, validateEntityName, validateFieldName, validateEnumName };
