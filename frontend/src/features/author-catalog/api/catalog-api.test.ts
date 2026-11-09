import {afterEach, describe, expect, it, vi} from 'vitest';
import {SystemError} from '@/shared/api';
import {json, pageResponse, stubFetch} from '@/shared/lib/test';
import {parseCatalogFilter} from '../model/catalog-filter';
import {fetchAuthorCatalogPage} from './catalog-api';

const PERSON = '00000000-0000-0000-0003-000000000001';
const COUNTRY = '00000000-0000-0000-0002-000000000001';
const CITY = '00000000-0000-0000-0002-000000000101';

describe(fetchAuthorCatalogPage, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('asks for a page of 100 entries without filters', async () => {
        const fetchMock = stubFetch(json(pageResponse([
            {personId: PERSON, name: 'Axe', realName: 'John Doe', labelOf: 'Zed'},
            {personId: PERSON, name: 'Zed', realName: null, labelOf: null},
        ], {size: 100})));

        const page = await fetchAuthorCatalogPage(parseCatalogFilter(new URLSearchParams()), new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/authors?page=0&size=100');
        expect(page.items).toStrictEqual([
            {personId: PERSON, name: 'Axe', realName: 'John Doe', labelOf: 'Zed'},
            {personId: PERSON, name: 'Zed', realName: null, labelOf: null},
        ]);
    });

    it('asks with the filters and the page', async () => {
        const fetchMock = stubFetch(json(pageResponse([])));

        await fetchAuthorCatalogPage(parseCatalogFilter(new URLSearchParams(`letter=%23&country=${COUNTRY}&city=${CITY}&page=2`)), new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/authors?page=1&size=100&letter=%23&country=${COUNTRY}&city=${CITY}`);
    });

    it('fails with a system error on a malformed answer', async () => {
        stubFetch(json(pageResponse([{personId: 'nope', name: 'Axe', realName: null, labelOf: null}])));

        await expect(fetchAuthorCatalogPage(parseCatalogFilter(new URLSearchParams()), new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
    });
});
