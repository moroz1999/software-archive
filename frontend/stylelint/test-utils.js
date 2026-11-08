import {mkdtempSync, mkdirSync, writeFileSync} from 'node:fs';
import {tmpdir} from 'node:os';
import {dirname, join} from 'node:path';
import stylelint from 'stylelint';

/** Creates a temporary directory with the given files: {relativePath: content}. */
export function tempFiles(files) {
    const root = mkdtempSync(join(tmpdir(), 'stylelint-archive-'));
    for (const [path, content] of Object.entries(files)) {
        const full = join(root, path);
        mkdirSync(dirname(full), {recursive: true});
        writeFileSync(full, content);
    }
    return root;
}

/** Lints code as if it were the given file, with one rule of the plugin enabled; returns warning texts. */
export async function lint(rule, options, code, filename) {
    const result = await stylelint.lint({
        code,
        codeFilename: filename,
        config: {
            plugins: [join(import.meta.dirname, 'plugin.js')],
            rules: {[rule]: options},
        },
    });
    const [first] = result.results;
    return first ? first.warnings.map((warning) => warning.text) : [];
}
