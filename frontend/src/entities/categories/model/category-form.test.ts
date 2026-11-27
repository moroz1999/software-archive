import {describe, expect, it} from 'vitest';
import {toCategoryFormValues, validateCategoryForm} from './category-form';

const GAMES = '00000000-0000-0000-0006-000000000001';
const ACTION = '00000000-0000-0000-0006-000000000002';

describe(validateCategoryForm, () => {
    it('turns filled values into a draft', () => {
        expect(validateCategoryForm({titles: {ru: 'Экшен', en: 'Action'}, parent: GAMES}))
            .toStrictEqual({ok: true, draft: {titles: {ru: 'Экшен', en: 'Action'}, parentId: GAMES}});
    });

    it('puts a category without a parent at the top', () => {
        expect(validateCategoryForm({titles: {ru: 'Серии', en: 'Series'}, parent: ''}))
            .toStrictEqual({ok: true, draft: {titles: {ru: 'Серии', en: 'Series'}, parentId: null}});
    });

    it('leaves out a title not given in a language other than Russian', () => {
        expect(validateCategoryForm({titles: {ru: 'Серии', en: ' '}, parent: ''}))
            .toStrictEqual({ok: true, draft: {titles: {ru: 'Серии'}, parentId: null}});
    });

    it('needs a Russian title', () => {
        expect(validateCategoryForm({titles: {ru: ' ', en: 'Action'}, parent: ''}))
            .toStrictEqual({ok: false, errors: {titles: {ru: 'required'}}});
    });

    it('needs every title to be at most 255 characters', () => {
        expect(validateCategoryForm({titles: {ru: 'a'.repeat(256), en: 'a'.repeat(256)}, parent: ''}))
            .toStrictEqual({ok: false, errors: {titles: {ru: 'too-long', en: 'too-long'}}});
    });
});

describe(toCategoryFormValues, () => {
    it('is empty for a new category', () => {
        expect(toCategoryFormValues()).toStrictEqual({titles: {en: '', ru: ''}, parent: ''});
    });

    it('holds the values of an existing category', () => {
        expect(toCategoryFormValues({id: ACTION, titles: {ru: 'Экшен', en: 'Action'}, parentId: GAMES}))
            .toStrictEqual({titles: {en: 'Action', ru: 'Экшен'}, parent: GAMES});
        expect(toCategoryFormValues({id: GAMES, titles: {ru: 'Игры'}, parentId: null}))
            .toStrictEqual({titles: {en: '', ru: 'Игры'}, parent: ''});
    });
});
