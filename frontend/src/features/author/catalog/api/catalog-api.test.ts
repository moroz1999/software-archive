import {afterEach, describe, expect, it, vi} from 'vitest';
import {json, pageResponse, stubFetch} from '@/shared/lib/test';
import {parseCatalogFilter} from '../model/catalog-filter';
import {fetchAuthorCatalogPage} from './catalog-api';

const PERSON = '00000000-0000-0000-0003-000000000001';
const LABEL = '00000000-0000-0000-0004-000000000001';
const COUNTRY = '00000000-0000-0000-0002-000000000001';
const CITY = '00000000-0000-0000-0002-000000000101';

describe(fetchAuthorCatalogPage, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('asks for a page of 100 entries without filters', async () => {
        const fetchMock = stubFetch(json(pageResponse([
            {personId: null, labelId: LABEL, name: 'Axe', note: 'John Doe'},
            {personId: PERSON, labelId: null, name: 'John Doe', note: 'Zed'},
        ], {size: 100})));

        const page = await fetchAuthorCatalogPage(parseCatalogFilter(new URLSearchParams()), null, new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/authors?page=0');
        expect(page.content).toStrictEqual([
            {kind: 'label', labelId: LABEL, name: 'Axe', note: 'John Doe'},
            {kind: 'person', personId: PERSON, name: 'John Doe', note: 'Zed'},
        ]);
    });

    it('asks with the filters and the page', async () => {
        const fetchMock = stubFetch(json(pageResponse([])));

        await fetchAuthorCatalogPage(parseCatalogFilter(new URLSearchParams(`letter=%23&country=${COUNTRY}&city=${CITY}&page=2`)), null, new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/authors?page=1&letter=%23&country=${COUNTRY}&city=${CITY}`);
    });

    it('asks only for the entries of the chosen platform', async () => {
        const fetchMock = stubFetch(json(pageResponse([])));

        await fetchAuthorCatalogPage(parseCatalogFilter(new URLSearchParams('letter=A')), 'bk', new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/authors?page=0&letter=A&platform=bk');
    });
});
