import {afterEach, describe, expect, it, vi} from 'vitest';
import {json, noContent, stubFetch} from '@/shared/lib/test';
import {createCategory, deleteCategory, fetchCategory, fetchCategoryTree, updateCategory} from './category-api';

const GAMES = '00000000-0000-0000-0006-000000000001';
const ACTION = '00000000-0000-0000-0006-000000000002';
const TREE = [
    {id: GAMES, title: 'Games', children: [{id: ACTION, title: 'Action', children: []}]},
    {id: '00000000-0000-0000-0006-000000000003', title: 'Demoscene', children: []},
];

describe(fetchCategoryTree, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('reads the whole tree', async () => {
        const fetchMock = stubFetch(json(TREE));

        await expect(fetchCategoryTree(new AbortController().signal)).resolves.toStrictEqual(TREE);
        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/categories');
    });
});

describe(fetchCategory, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('reads one category with its titles in every language and its parent', async () => {
        const fetchMock = stubFetch(json({id: ACTION, titles: {ru: 'Экшен', en: 'Action'}, parentId: GAMES}));

        await expect(fetchCategory(ACTION, new AbortController().signal))
            .resolves.toStrictEqual({id: ACTION, titles: {ru: 'Экшен', en: 'Action'}, parentId: GAMES});
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/categories/${ACTION}`);
    });

    it('reads a category at the top without a parent', async () => {
        stubFetch(json({id: GAMES, titles: {ru: 'Игры'}, parentId: null}));

        await expect(fetchCategory(GAMES, new AbortController().signal)).resolves.toStrictEqual({id: GAMES, titles: {ru: 'Игры'}, parentId: null});
    });

    it('refuses a category without a Russian title', async () => {
        stubFetch(json({id: GAMES, titles: {en: 'Games'}, parentId: null}));

        await expect(fetchCategory(GAMES, new AbortController().signal)).rejects.toThrow('A category has a Russian title.');
    });

    it('takes no title in a language the site does not speak', async () => {
        stubFetch(json({id: GAMES, titles: {ru: 'Игры', de: 'Spiele'}, parentId: null}));

        await expect(fetchCategory(GAMES, new AbortController().signal)).resolves.toStrictEqual({id: GAMES, titles: {ru: 'Игры'}, parentId: null});
    });

    it('passes a not found error on', async () => {
        stubFetch(json({status: 404, code: 'category.not-found'}, 404));

        await expect(fetchCategory(ACTION, new AbortController().signal)).rejects.toMatchObject({name: 'NotFoundError'});
    });
});

describe(createCategory, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('sends the draft and returns the new id', async () => {
        const fetchMock = stubFetch(json({id: ACTION}, 201));

        await expect(createCategory({titles: {ru: 'Экшен', en: 'Action'}, parentId: GAMES})).resolves.toBe(ACTION);
        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/categories');
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'POST', body: `{"titles":{"ru":"Экшен","en":"Action"},"parentId":"${GAMES}"}`});
    });
});

describe(updateCategory, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('sends the whole category', async () => {
        const fetchMock = stubFetch(noContent());

        await updateCategory(ACTION, {titles: {ru: 'Экшен-игры'}, parentId: null});

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/categories/${ACTION}`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'PUT', body: '{"titles":{"ru":"Экшен-игры"},"parentId":null}'});
    });
});

describe(deleteCategory, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('deletes the category', async () => {
        const fetchMock = stubFetch(noContent());

        await deleteCategory(ACTION);

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/categories/${ACTION}`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'DELETE'});
    });
});
