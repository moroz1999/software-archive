import {afterEach, describe, expect, it, vi} from 'vitest';
import {SystemError} from '@/shared/api';
import {json, noContent, stubFetch} from '@/shared/lib/test';
import {toCategoryId} from '../model/category';
import {createCategory, deleteCategory, fetchCategory, fetchCategoryTree, updateCategory} from './category-api';

const GAMES = toCategoryId('00000000-0000-0000-0006-000000000001');
const ACTION = toCategoryId('00000000-0000-0000-0006-000000000002');
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

    it('fails with a system error on a malformed answer', async () => {
        stubFetch(json([{id: GAMES, title: 'Games', children: null}]));

        await expect(fetchCategoryTree(new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
    });
});

describe(fetchCategory, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('reads one category with its parent', async () => {
        const fetchMock = stubFetch(json({id: ACTION, title: 'Action', parentId: GAMES}));

        await expect(fetchCategory(ACTION, new AbortController().signal)).resolves.toStrictEqual({id: ACTION, title: 'Action', parentId: GAMES});
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/categories/${ACTION}`);
    });

    it('reads a category at the top without a parent', async () => {
        stubFetch(json({id: GAMES, title: 'Games', parentId: null}));

        await expect(fetchCategory(GAMES, new AbortController().signal)).resolves.toStrictEqual({id: GAMES, title: 'Games', parentId: null});
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

        await expect(createCategory({title: 'Action', parentId: GAMES})).resolves.toBe(ACTION);
        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/categories');
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'POST', body: `{"title":"Action","parentId":"${GAMES}"}`});
    });
});

describe(updateCategory, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('sends the whole category', async () => {
        const fetchMock = stubFetch(noContent());

        await updateCategory(ACTION, {title: 'Action games', parentId: null});

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/categories/${ACTION}`);
        expect(fetchMock.mock.calls[0]?.[1]).toMatchObject({method: 'PUT', body: '{"title":"Action games","parentId":null}'});
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
