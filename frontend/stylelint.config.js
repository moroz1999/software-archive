// Design system and layout rules from docs/styles.md, enforced on every CSS file.
import {join} from 'node:path';

const themeDir = join(import.meta.dirname, 'src/app/styles/components');

const noNegativeMargins = {
    '/^margin/': ['/(^|\\s)-/', '/calc\\(\\s*-/'],
};

const layout = {
    ...noNegativeMargins,
    display: ['flex', 'inline-flex', 'grid', 'inline-grid'],
};

export default {
    extends: ['stylelint-config-standard', 'stylelint-config-css-modules'],
    plugins: ['stylelint-declaration-strict-value', './stylelint/plugin.js'],
    ignoreFiles: ['dist/**', 'coverage/**'],
    rules: {
        // CSS module classes are read as properties of the imported object: styles.gapMd.
        'selector-class-pattern': ['^[a-z][a-zA-Z0-9]*$', {message: 'Use a camelCase class name'}],
        'keyframes-name-pattern': ['^[a-z][a-zA-Z0-9]*$', {message: 'Use a camelCase keyframes name'}],
        'custom-property-pattern': ['^[a-z][a-z0-9]*(-[a-z0-9]+)*$', {message: 'Use a kebab-case custom property'}],

        // A component's CSS uses only its own variables, mapped onto the theme in its theme file.
        'archive/component-variables': [true, {themeDir}],

        // No raw design values in components: they all come through component variables.
        'scale-unlimited/declaration-strict-value': [
            [
                '/color$/', 'fill', 'stroke', 'background',
                '/^margin/', '/^padding/', '/gap$/',
                'box-shadow', 'border', '/^border-(top|right|bottom|left)$/', '/^border-.*width$/', 'border-radius',
                'outline', 'outline-offset',
                'font-size', 'font-family', 'font-weight', 'line-height',
                'width', 'height', '/^(min|max)-(width|height)$/',
                'opacity', 'z-index',
            ],
            {
                ignoreValues: [
                    'inherit', 'initial', 'unset', 'currentcolor', 'transparent', 'none', '0', 'auto', '100%',
                    'fit-content', 'min-content', 'max-content',
                    // Geometric constants, not design values: a circle, full opacity, a unit line height.
                    '50%', '1',
                ],
                disableFix: true,
            },
        ],
        'color-no-hex': true,
        'color-named': 'never',

        // Business components (pages, widgets, features, entities) never do layout in CSS:
        // they use Stack, Inline, Grid, Inset and Panel from shared/ui.
        'declaration-property-value-disallowed-list': layout,
        'property-disallowed-list': ['/^padding/', 'gap', 'row-gap', 'column-gap'],

        // Global styles live only in app/.
        'selector-pseudo-class-disallowed-list': ['global'],
    },
    overrides: [
        {
            // Atomic design system components own layout.
            files: ['src/shared/ui/**/*.css'],
            rules: {
                'declaration-property-value-disallowed-list': noNegativeMargins,
                'property-disallowed-list': null,
            },
        },
        {
            // Global styles: palette, tokens, themes, component theme files and reset.
            files: ['src/app/**/*.css'],
            rules: {
                'declaration-property-value-disallowed-list': noNegativeMargins,
                'property-disallowed-list': null,
                'selector-pseudo-class-disallowed-list': null,
                'color-no-hex': null,
                'scale-unlimited/declaration-strict-value': null,
            },
        },
        {
            // Palette, tokens and semantic theme: custom properties only.
            files: ['src/app/styles/palette.css', 'src/app/styles/tokens.css', 'src/app/styles/themes.css'],
            rules: {
                'archive/theme-file': [true, {prefix: false}],
            },
        },
        {
            // Component theme files: only --{component}-* custom properties, each with a comment.
            // Colors come only from the palette and the semantic theme.
            files: ['src/app/styles/components/*.css'],
            rules: {
                'archive/theme-file': [true, {prefix: true}],
                'color-no-hex': true,
                'function-disallowed-list': ['rgb', 'rgba', 'hsl', 'hsla', 'hwb', 'lab', 'lch', 'oklab', 'oklch', 'color'],
            },
        },
        {
            // The components index imports every component theme file.
            files: ['src/app/styles/components/index.css'],
            rules: {
                'archive/theme-file': null,
                'archive/theme-imports': true,
            },
        },
    ],
};
