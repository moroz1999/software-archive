// @vitest-environment node
import {join} from 'node:path';
import {describe, expect, it} from 'vitest';
import {lint, tempFiles} from './test-utils.js';

const rule = 'archive/theme-imports';

const root = tempFiles({
    'components/button.css': '',
    'components/panel.css': '',
});
const index = join(root, 'components/index.css');

describe('archive/theme-imports', () => {
    it('accepts an index importing every theme file of its directory', async () => {
        expect(await lint(rule, true, '@import url("./button.css");\n@import url("./panel.css");\n', index)).toStrictEqual([]);
    });

    it('reports a theme file that is not imported', async () => {
        expect(await lint(rule, true, '@import url("./button.css");\n', index)).toStrictEqual([
            'Theme file panel.css is not imported (archive/theme-imports)',
        ]);
    });

    it('reports an import of a missing theme file', async () => {
        expect(await lint(rule, true, '@import url("./button.css");\n@import url("./panel.css");\n@import url("./chip.css");\n', index)).toStrictEqual([
            'Imported theme file chip.css does not exist (archive/theme-imports)',
        ]);
    });
});
