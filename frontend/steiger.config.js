import {defineConfig} from 'steiger';
import fsd from '@feature-sliced/steiger-plugin';

export default defineConfig([
    ...fsd.configs.recommended,
    {
        // Early-stage slices are often used by a single page; merging them back would fight the layer structure.
        // Slices are named `{entity}-{action}` (software-list, software-edit), so a shared prefix is the convention.
        rules: {
            'fsd/insignificant-slice': 'off',
            'fsd/repetitive-naming': 'off',
        },
    },
]);
