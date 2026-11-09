import js from '@eslint/js';
import globals from 'globals';
import react from 'eslint-plugin-react';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import jsxA11y from 'eslint-plugin-jsx-a11y';
import vitest from '@vitest/eslint-plugin';
import testingLibrary from 'eslint-plugin-testing-library';
import tseslint from 'typescript-eslint';

/** How modules reach each other (see docs/frontend.md): `@/` across layers and slices, no barrels in shared/ui and shared/lib. */
const importPathPatterns = [
    {
        regex: '^@/shared/(ui|lib)$',
        message: 'shared/ui and shared/lib have no barrels: import the component folder (@/shared/ui/button) or the module (@/shared/lib/context).',
    },
    {
        regex: '^(\\.\\./){2,}',
        message: 'Reach other slices and segments from the root: @/layer/...',
    },
    {
        regex: '^(\\.\\./)+(app|pages|widgets|features|entities|shared)(/|$)',
        message: 'Reach other layers from the root: @/layer/...',
    },
];

export default tseslint.config(
    {ignores: ['dist', 'src/**/*.module.css.d.ts']},

    // Base: strictest type-aware presets.
    {
        files: ['**/*.{ts,tsx}'],
        extends: [
            js.configs.recommended,
            ...tseslint.configs.strictTypeChecked,
            ...tseslint.configs.stylisticTypeChecked,
            react.configs.flat.recommended,
            react.configs.flat['jsx-runtime'],
            jsxA11y.flatConfigs.strict,
        ],
        languageOptions: {
            ecmaVersion: 2022,
            globals: globals.browser,
            parserOptions: {
                projectService: true,
                tsconfigRootDir: import.meta.dirname,
            },
        },
        settings: {
            react: {version: 'detect'},
        },
        plugins: {
            'react-hooks': reactHooks,
            'react-refresh': reactRefresh,
        },
        rules: {
            ...reactHooks.configs.recommended.rules,
            'react-refresh/only-export-components': ['error', {allowConstantExport: true}],

            // Named exports only: a slice exposes its API through index.ts by name.
            'no-restricted-syntax': ['error', {
                selector: 'ExportDefaultDeclaration',
                message: 'Use named exports.',
            }],

            // Branded value objects are created only by their constructor functions (see docs/value-objects.md).
            '@typescript-eslint/consistent-type-assertions': ['error', {assertionStyle: 'never'}],

            // HTTP goes only through the shared API client: it classifies errors (see docs/error-handling.md).
            'no-restricted-globals': ['error',
                {name: 'fetch', message: 'Use the API client from shared/api.'},
                {name: 'XMLHttpRequest', message: 'Use the API client from shared/api.'},
            ],

            // Global CSS only in app/; components use CSS Modules.
            'no-restricted-imports': ['error', {
                patterns: [
                    ...importPathPatterns,
                    {
                        regex: '^(?!.*\\.module\\.css$).*\\.css$',
                        message: 'Import only *.module.css; global styles live in app/.',
                    },
                ],
            }],

            // Styling goes through CSS Modules and the design system (see docs/frontend.md, docs/design-system.md).
            'react/forbid-dom-props': ['error', {forbid: ['style']}],
            'react/forbid-elements': ['error', {
                forbid: [
                    {element: 'button', message: 'Use the Button component from shared/ui.'},
                    {element: 'a', message: 'Use the Button component with href from shared/ui.'},
                ],
            }],
        },
    },

    // Atomic design system components own native markup.
    {
        files: ['src/shared/ui/**/*.{ts,tsx}'],
        rules: {
            'react/forbid-elements': 'off',
        },
    },

    // The API client is the only HTTP caller.
    {
        files: ['src/shared/api/**/*.ts'],
        rules: {
            'no-restricted-globals': 'off',
        },
    },

    // Global styles are imported by the app layer.
    {
        files: ['src/app/**/*.{ts,tsx}'],
        rules: {
            'no-restricted-imports': ['error', {patterns: importPathPatterns}],
        },
    },

    // Value object constructors are the only place allowed to brand a raw value.
    {
        files: ['src/**/model/**/*.ts', 'src/shared/lib/**/*.ts'],
        rules: {
            '@typescript-eslint/consistent-type-assertions': ['error', {
                assertionStyle: 'as',
                objectLiteralTypeAssertions: 'never',
            }],
        },
    },

    // Tests.
    {
        files: ['src/**/*.test.{ts,tsx}'],
        extends: [
            vitest.configs.all,
            testingLibrary.configs['flat/react'],
        ],
        settings: {
            vitest: {typecheck: true},
        },
        rules: {
            // Tests are flat and explicit; no shared hooks, titles in plain sentences.
            'vitest/prefer-expect-assertions': 'off',
            'vitest/no-hooks': 'off',
            'vitest/prefer-lowercase-title': 'off',
            'vitest/require-mock-type-parameters': 'off',
            // Conflicts with vitest/prefer-called-once from the same preset.
            'vitest/prefer-called-times': 'off',
        },
    },

    // Design system atoms promise markup (element, classes, attributes): their tests read the DOM node itself.
    {
        files: ['src/shared/ui/**/*.test.tsx'],
        rules: {
            'testing-library/no-node-access': 'off',
            'testing-library/no-container': 'off',
        },
    },

    // Tool configs require default exports.
    {
        files: ['*.config.{ts,js}'],
        rules: {
            'no-restricted-syntax': 'off',
        },
    },

    // Plain JS files are not part of any tsconfig.
    {
        files: ['**/*.js'],
        extends: [js.configs.recommended, tseslint.configs.disableTypeChecked],
        languageOptions: {globals: globals.node},
        rules: {
            // Tool configs require default exports.
            'no-restricted-syntax': 'off',
        },
    },
);
