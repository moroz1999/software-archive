// @vitest-environment node
import {describe, expect, it} from 'vitest';
import {lint} from './test-utils.js';

const rule = 'archive/theme-file';
const componentFile = '/styles/components/button.css';

function lintComponentTheme(code) {
    return lint(rule, [true, {prefix: true}], code, componentFile);
}

const dark = (body) => `@media (prefers-color-scheme: dark) {\n    :root {\n${body}\n    }\n}\n`;

describe('archive/theme-file', () => {
    it('accepts commented custom properties in :root and in the dark media query', async () => {
        const code = `:root {\n    /* Background. */\n    --button-bg: var(--primary-500);\n}\n\n${dark('        --button-bg: var(--primary-400);')}`;
        expect(await lintComponentTheme(code)).toStrictEqual([]);
    });

    it('rejects an ordinary property', async () => {
        expect(await lintComponentTheme(':root {\n    /* Color. */\n    color: red;\n}\n')).toStrictEqual([
            'A theme file declares only custom properties, not "color" (archive/theme-file)',
        ]);
    });

    it('rejects a selector other than :root', async () => {
        expect(await lintComponentTheme('.dark-mode {\n    /* Background. */\n    --button-bg: var(--primary-400);\n}\n')).toStrictEqual([
            'A theme file contains only :root rules, not ".dark-mode" (archive/theme-file)',
        ]);
    });

    it('rejects an at-rule other than the dark color scheme media query', async () => {
        expect(await lintComponentTheme('@media (width <= 600px) {\n    :root {\n        --button-bg: red;\n    }\n}\n')).toStrictEqual([
            'A theme file allows only @media (prefers-color-scheme: dark), not "@media (width <= 600px)" (archive/theme-file)',
        ]);
    });

    it('rejects a variable of another component', async () => {
        expect(await lintComponentTheme(':root {\n    /* Background. */\n    --panel-bg: var(--white);\n}\n')).toStrictEqual([
            'button.css declares only --button-* variables, not --panel-bg (archive/theme-file)',
        ]);
    });

    it('rejects an uncommented variable in :root', async () => {
        expect(await lintComponentTheme(':root {\n    --button-bg: var(--white);\n}\n')).toStrictEqual([
            '--button-bg needs a comment explaining its purpose (archive/theme-file)',
        ]);
    });

    it('accepts a group of adjacent variables under one comment', async () => {
        const code = ':root {\n    /* Medium size: height and padding. */\n    --button-md-height: 36px;\n    --button-md-padding: 0 var(--space-16);\n}\n';
        expect(await lintComponentTheme(code)).toStrictEqual([]);
    });

    it('rejects a variable separated from the commented group by a blank line', async () => {
        const code = ':root {\n    /* Height. */\n    --button-md-height: 36px;\n\n    --button-md-padding: 0;\n}\n';
        expect(await lintComponentTheme(code)).toStrictEqual([
            '--button-md-padding needs a comment explaining its purpose (archive/theme-file)',
        ]);
    });

    it('accepts any variable name in a global theme file', async () => {
        expect(await lint(rule, [true, {prefix: false}], ':root {\n    /* Text. */\n    --text-color: var(--black);\n}\n', '/styles/themes.css')).toStrictEqual([]);
    });
});
