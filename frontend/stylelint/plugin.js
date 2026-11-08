// Local Stylelint rules for the component variable hierarchy (docs/styles.md).
import {existsSync, readdirSync, readFileSync} from 'node:fs';
import {basename, dirname, join} from 'node:path';
import stylelint from 'stylelint';

const {createPlugin, utils: {report, ruleMessages, validateOptions}} = stylelint;

const VAR_REFERENCE = /var\(\s*(--[\w-]+)/g;
const DARK_SCHEME = /^\(\s*prefers-color-scheme:\s*dark\s*\)$/;
const IMPORT_PATH = /^(?:url\()?\s*["']?\.\/([^"')]+)["']?\s*\)?$/;

function kebab(name) {
    return name.replace(/([a-z0-9])([A-Z])/g, '$1-$2').toLowerCase();
}

function fileOf(root) {
    return root.source?.input.file;
}

function declaredVariables(css) {
    return new Set([...css.matchAll(/(--[\w-]+)\s*:/g)].map((match) => match[1]));
}

const themeCache = new Map();

function themeVariables(path) {
    if (!themeCache.has(path)) {
        themeCache.set(path, existsSync(path) ? declaredVariables(readFileSync(path, 'utf8')) : new Set());
    }
    return themeCache.get(path);
}

// --- archive/component-variables -------------------------------------------------------------

const componentVariablesName = 'archive/component-variables';
const componentVariablesMessages = ruleMessages(componentVariablesName, {
    foreign: (file, prefix, variable) => `Use only ${prefix}* variables in ${file}, not ${variable}`,
    undeclared: (variable, file, theme) => `${variable} is declared neither in ${file} nor in its theme file ${theme}`,
});

/**
 * A CSS module of component X uses and declares only --{kebab(X)}-* variables,
 * and each one it uses is declared in the module or in the component's theme file.
 */
const componentVariables = createPlugin(componentVariablesName, (enabled, options) => (root, result) => {
    if (!validateOptions(result, componentVariablesName,
        {actual: enabled},
        {actual: options, possible: {themeDir: [(value) => typeof value === 'string']}})) {
        return;
    }
    const file = fileOf(root);
    if (file === undefined || !file.endsWith('.module.css')) {
        return;
    }
    const moduleName = basename(file);
    const component = kebab(moduleName.slice(0, -'.module.css'.length));
    const prefix = `--${component}-`;
    const themeName = `${component}.css`;
    const local = declaredVariables(root.toString());
    const theme = themeVariables(join(options.themeDir, themeName));

    root.walkDecls((decl) => {
        if (decl.prop.startsWith('--') && !decl.prop.startsWith(prefix)) {
            report({ruleName: componentVariablesName, result, node: decl,
                message: componentVariablesMessages.foreign(moduleName, prefix, decl.prop)});
        }
        for (const [, variable] of decl.value.matchAll(VAR_REFERENCE)) {
            if (!variable.startsWith(prefix)) {
                report({ruleName: componentVariablesName, result, node: decl,
                    message: componentVariablesMessages.foreign(moduleName, prefix, variable)});
            } else if (!local.has(variable) && !theme.has(variable)) {
                report({ruleName: componentVariablesName, result, node: decl,
                    message: componentVariablesMessages.undeclared(variable, moduleName, themeName)});
            }
        }
    });
});

/** A declaration is commented when a comment precedes it directly or through adjacent declarations of its group. */
function isCommented(decl) {
    let node = decl;
    for (;;) {
        const previous = node.prev();
        if (previous === undefined) {
            return false;
        }
        if (previous.type === 'comment') {
            return true;
        }
        if (previous.type !== 'decl' || /\n\s*\n/.test(node.raws.before ?? '')) {
            return false;
        }
        node = previous;
    }
}

// --- archive/theme-file ----------------------------------------------------------------------

const themeFileName = 'archive/theme-file';
const themeFileMessages = ruleMessages(themeFileName, {
    property: (prop) => `A theme file declares only custom properties, not "${prop}"`,
    selector: (selector) => `A theme file contains only :root rules, not "${selector}"`,
    atRule: (atRule) => `A theme file allows only @media (prefers-color-scheme: dark), not "${atRule}"`,
    prefix: (file, prefix, variable) => `${file} declares only ${prefix}* variables, not ${variable}`,
    comment: (variable) => `${variable} needs a comment explaining its purpose`,
});

/**
 * A theme file holds only custom property declarations in :root, light by default and
 * dark under @media (prefers-color-scheme: dark). With `prefix`, it is a component theme file:
 * it declares only --{file name}-* variables, each with a comment (shared by a group of adjacent ones).
 */
const themeFile = createPlugin(themeFileName, (enabled, options) => (root, result) => {
    if (!validateOptions(result, themeFileName,
        {actual: enabled},
        {actual: options, possible: {prefix: [true, false]}, optional: true})) {
        return;
    }
    const file = fileOf(root);
    const withPrefix = options?.prefix === true && file !== undefined;
    const fileName = file === undefined ? '' : basename(file);
    const prefix = `--${fileName.replace(/\.css$/, '')}-`;

    function checkRule(rule, requireComments) {
        if (rule.selector !== ':root') {
            report({ruleName: themeFileName, result, node: rule, message: themeFileMessages.selector(rule.selector)});
            return;
        }
        rule.each((node) => {
            if (node.type === 'comment') {
                return;
            }
            if (node.type !== 'decl' || !node.prop.startsWith('--')) {
                report({ruleName: themeFileName, result, node,
                    message: themeFileMessages.property(node.type === 'decl' ? node.prop : node.toString())});
                return;
            }
            if (withPrefix && !node.prop.startsWith(prefix)) {
                report({ruleName: themeFileName, result, node, message: themeFileMessages.prefix(fileName, prefix, node.prop)});
            }
            if (withPrefix && requireComments && !isCommented(node)) {
                report({ruleName: themeFileName, result, node, message: themeFileMessages.comment(node.prop)});
            }
        });
    }

    root.each((node) => {
        if (node.type === 'comment') {
            return;
        }
        if (node.type === 'rule') {
            checkRule(node, true);
            return;
        }
        if (node.type === 'atrule' && node.name === 'media' && DARK_SCHEME.test(node.params.trim())) {
            node.each((child) => {
                if (child.type === 'rule') {
                    checkRule(child, false);
                } else if (child.type !== 'comment') {
                    report({ruleName: themeFileName, result, node: child, message: themeFileMessages.selector(child.toString())});
                }
            });
            return;
        }
        report({ruleName: themeFileName, result, node,
            message: themeFileMessages.atRule(node.type === 'atrule' ? `@${node.name} ${node.params}` : node.toString())});
    });
});

// --- archive/theme-imports -------------------------------------------------------------------

const themeImportsName = 'archive/theme-imports';
const themeImportsMessages = ruleMessages(themeImportsName, {
    missing: (file) => `Theme file ${file} is not imported`,
    unknown: (file) => `Imported theme file ${file} does not exist`,
});

/** The index of a theme directory imports every theme file in that directory, and only existing ones. */
const themeImports = createPlugin(themeImportsName, (enabled) => (root, result) => {
    if (!validateOptions(result, themeImportsName, {actual: enabled})) {
        return;
    }
    const file = fileOf(root);
    if (file === undefined) {
        return;
    }
    const indexName = basename(file);
    const present = new Set(readdirSync(dirname(file)).filter((name) => name.endsWith('.css') && name !== indexName));
    const imported = new Set();

    root.walkAtRules('import', (atRule) => {
        const path = IMPORT_PATH.exec(atRule.params.trim())?.[1];
        if (path === undefined) {
            return;
        }
        imported.add(path);
        if (!present.has(path)) {
            report({ruleName: themeImportsName, result, node: atRule, message: themeImportsMessages.unknown(path)});
        }
    });
    for (const name of [...present].sort()) {
        if (!imported.has(name)) {
            report({ruleName: themeImportsName, result, node: root, message: themeImportsMessages.missing(name)});
        }
    }
});

export default [componentVariables, themeFile, themeImports];
