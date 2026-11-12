import {defineConfig} from 'steiger';
import fsd from '@feature-sliced/steiger-plugin';

export default defineConfig([
    ...fsd.configs.recommended,
    {
        // Early-stage slices are often used by a single page; merging them back would fight the layer structure.
        // Features are grouped by entity (features/software/edit, features/author/edit), so slice names repeat.
        rules: {
            'fsd/insignificant-slice': 'off',
            'fsd/repetitive-naming': 'off',
        },
    },
]);
