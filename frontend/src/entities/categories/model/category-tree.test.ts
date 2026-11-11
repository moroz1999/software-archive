import {describe, expect, it} from 'vitest';
import {type CategoryNode, toCategoryId} from './category';
import {parentOptions} from './category-tree';

const GAMES = toCategoryId('00000000-0000-0000-0006-000000000001');
const ACTION = toCategoryId('00000000-0000-0000-0006-000000000002');
const SHOOTERS = toCategoryId('00000000-0000-0000-0006-000000000003');
const DEMOSCENE = toCategoryId('00000000-0000-0000-0006-000000000004');

const TREE: readonly CategoryNode[] = [
    {id: GAMES, title: 'Games', children: [{id: ACTION, title: 'Action', children: [{id: SHOOTERS, title: 'Shooters', children: []}]}]},
    {id: DEMOSCENE, title: 'Demoscene', children: []},
];

describe(parentOptions, () => {
    it('lists every category in the order of the tree, with its depth', () => {
        expect(parentOptions(TREE)).toStrictEqual([
            {id: GAMES, title: 'Games', depth: 0},
            {id: ACTION, title: 'Action', depth: 1},
            {id: SHOOTERS, title: 'Shooters', depth: 2},
            {id: DEMOSCENE, title: 'Demoscene', depth: 0},
        ]);
    });

    it('leaves out the category itself and its subcategories', () => {
        expect(parentOptions(TREE, ACTION)).toStrictEqual([
            {id: GAMES, title: 'Games', depth: 0},
            {id: DEMOSCENE, title: 'Demoscene', depth: 0},
        ]);
    });
});
