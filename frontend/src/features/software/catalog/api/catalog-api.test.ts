import {afterEach, describe, expect, it, vi} from 'vitest';
import {toPlatformCode} from '@/entities/platforms';
import {SystemError} from '@/shared/api';
import {json, pageResponse, stubFetch} from '@/shared/lib/test';
import {parseCatalogFilter} from '../model/catalog-filter';
import {fetchSoftwareCatalog} from './catalog-api';

const ELITE = '00000000-0000-0000-00fd-000000000001';
const GAMES = '00000000-0000-0000-00fd-0000000000c1';
const FINLAND = '00000000-0000-0000-00fd-0000000000f1';

const FACETS = {
    years: [{value: 1987, count: 2}],
    categories: [{value: GAMES, count: 3}],
    countries: [{value: FINLAND, count: 1}],
};

function catalogResponse(items: readonly unknown[], facets: unknown = FACETS) {
    return json({...pageResponse(items, {totalElements: 41, totalPages: 3}), facets});
}

function filterOf(search: string, category?: string) {
    return parseCatalogFilter(category, new URLSearchParams(search));
}

describe(fetchSoftwareCatalog, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('asks for the first page of 20 without a keyword or filters', async () => {
        const fetchMock = stubFetch(catalogResponse([]));

        await fetchSoftwareCatalog(filterOf(''), null, new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/software?page=0&size=20');
    });

    it('asks with the keyword, the filters, the page and the platform', async () => {
        const fetchMock = stubFetch(catalogResponse([]));

        await fetchSoftwareCatalog(
            filterOf(`q=elite%202&year=1987&country=${FINLAND}&page=2`, GAMES),
            toPlatformCode('bk'),
            new AbortController().signal,
        );

        expect(fetchMock.mock.calls[0]?.[0]).toBe(
            `/api/software?page=1&size=20&q=elite+2&year=1987&category=${GAMES}&country=${FINLAND}&platform=bk`,
        );
    });

    it('reads the cards, the page and the values of every filter', async () => {
        stubFetch(catalogResponse([{id: ELITE, title: 'Elite', releaseDate: '1987-05-01'}]));

        const catalog = await fetchSoftwareCatalog(filterOf(''), null, new AbortController().signal);

        expect(catalog.page).toMatchObject({number: 0, totalElements: 41, totalPages: 3});
        expect(catalog.page.items).toStrictEqual([{id: ELITE, title: 'Elite', releaseDate: '1987-05-01'}]);
        expect(catalog.facets).toStrictEqual(FACETS);
    });

    it('fails with a system error on malformed values of a filter', async () => {
        stubFetch(catalogResponse([], {years: [{value: '1987', count: 2}], categories: [], countries: []}));

        await expect(fetchSoftwareCatalog(filterOf(''), null, new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
    });

    it('fails with a system error on a category that is no id', async () => {
        stubFetch(catalogResponse([], {years: [], categories: [{value: 'games', count: 2}], countries: []}));

        await expect(fetchSoftwareCatalog(filterOf(''), null, new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
    });
});
