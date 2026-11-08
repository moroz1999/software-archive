// @vitest-environment node
import {join} from 'node:path';
import {describe, expect, it} from 'vitest';
import {lint, tempFiles} from './test-utils.js';

const rule = 'archive/component-variables';

const root = tempFiles({
    'components/button.css': ':root {\n    /* Background. */\n    --button-bg: var(--primary-500);\n}\n',
    'components/software-list-item.css': ':root {\n    /* Title color. */\n    --software-list-item-title-color: var(--text-color);\n}\n',
});
const options = [true, {themeDir: join(root, 'components')}];

function lintModule(code, name = 'Button') {
    return lint(rule, options, code, join(root, `ui/${name}.module.css`));
}

describe('archive/component-variables', () => {
    it('accepts the component\'s own variable declared in its theme file', async () => {
        expect(await lintModule('.button { background: var(--button-bg); }')).toStrictEqual([]);
    });

    it('rejects a base token', async () => {
        expect(await lintModule('.button { gap: var(--space-8); }')).toStrictEqual([
            'Use only --button-* variables in Button.module.css, not --space-8 (archive/component-variables)',
        ]);
    });

    it('rejects a variable of another component', async () => {
        expect(await lintModule('.button { background: var(--panel-bg); }')).toStrictEqual([
            'Use only --button-* variables in Button.module.css, not --panel-bg (archive/component-variables)',
        ]);
    });

    it('rejects a base token used as a fallback', async () => {
        expect(await lintModule('.button { background: var(--button-bg, var(--white)); }')).toStrictEqual([
            'Use only --button-* variables in Button.module.css, not --white (archive/component-variables)',
        ]);
    });

    it('rejects an own variable that is declared nowhere', async () => {
        expect(await lintModule('.button { color: var(--button-text); }')).toStrictEqual([
            '--button-text is declared neither in Button.module.css nor in its theme file button.css (archive/component-variables)',
        ]);
    });

    it('accepts an own variable declared in the module itself', async () => {
        expect(await lintModule('.ripple { --button-ripple-x: 0; left: var(--button-ripple-x); }')).toStrictEqual([]);
    });

    it('rejects declaring a variable without the component prefix', async () => {
        expect(await lintModule('.button { --ripple-x: 0; }')).toStrictEqual([
            'Use only --button-* variables in Button.module.css, not --ripple-x (archive/component-variables)',
        ]);
    });

    it('derives a kebab-case prefix from a multi-word component name', async () => {
        expect(await lintModule('.title { color: var(--software-list-item-title-color); }', 'SoftwareListItem')).toStrictEqual([]);
    });

    it('rejects a module whose component has no theme file', async () => {
        expect(await lintModule('.card { color: var(--card-color); }', 'Card')).toStrictEqual([
            '--card-color is declared neither in Card.module.css nor in its theme file card.css (archive/component-variables)',
        ]);
    });

    it('ignores files that are not CSS modules', async () => {
        expect(await lint(rule, options, 'body { color: var(--text-color); }', join(root, 'reset.css'))).toStrictEqual([]);
    });
});
