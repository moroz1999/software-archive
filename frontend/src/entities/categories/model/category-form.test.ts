import {describe, expect, it} from 'vitest';
import {toCategoryId} from './category';
import {toCategoryFormValues, validateCategoryForm} from './category-form';

const GAMES = toCategoryId('00000000-0000-0000-0006-000000000001');
const ACTION = toCategoryId('00000000-0000-0000-0006-000000000002');

describe(validateCategoryForm, () => {
    it('turns filled values into a draft', () => {
        expect(validateCategoryForm({title: 'Action', parent: GAMES})).toStrictEqual({ok: true, draft: {title: 'Action', parentId: GAMES}});
    });

    it('puts a category without a parent at the top', () => {
        expect(validateCategoryForm({title: 'Series', parent: ''})).toStrictEqual({ok: true, draft: {title: 'Series', parentId: null}});
    });

    it('needs a title of at most 255 characters', () => {
        expect(validateCategoryForm({title: ' ', parent: ''})).toStrictEqual({ok: false, errors: {title: 'required'}});
        expect(validateCategoryForm({title: 'a'.repeat(256), parent: ''})).toStrictEqual({ok: false, errors: {title: 'too-long'}});
    });
});

describe(toCategoryFormValues, () => {
    it('is empty for a new category', () => {
        expect(toCategoryFormValues()).toStrictEqual({title: '', parent: ''});
    });

    it('holds the values of an existing category', () => {
        expect(toCategoryFormValues({id: ACTION, title: 'Action', parentId: GAMES})).toStrictEqual({title: 'Action', parent: GAMES});
        expect(toCategoryFormValues({id: GAMES, title: 'Games', parentId: null})).toStrictEqual({title: 'Games', parent: ''});
    });
});
