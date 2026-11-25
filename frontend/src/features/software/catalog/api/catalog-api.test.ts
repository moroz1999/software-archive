import {afterEach, describe, expect, it, vi} from 'vitest';
import {toPlatformCode} from '@/entities/platforms';
import {SystemError} from '@/shared/api';
import {json, pageResponse, stubFetch} from '@/shared/lib/test';
import {parseCatalogFilter} from '../model/catalog-filter';
import {fetchSoftwareCatalog} from './catalog-api';

const ELITE = '00000000-0000-0000-00fd-000000000001';
const GAMES = '00000000-0000-0000-00fd-0000000000c1';
const FINLAND = '00000000-0000-0000-00fd-0000000000f1';
const HELSINKI = '00000000-0000-0000-00fd-0000000000e1';

const FACETS = {
    years: [{value: 1987, count: 2}],
    countries: [{value: FINLAND, count: 1}],
    cities: [{value: HELSINKI, count: 1}],
    platforms: [{value: 'vector', count: 4}],
    hardware: [{value: 'ay8910', name: 'AY-3-8910', group: 'sound', groupName: 'Sound', count: 2}],
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

    it('asks for the first page of 30, full rows of 3 cards, without a keyword or filters', async () => {
        const fetchMock = stubFetch(catalogResponse([]));

        await fetchSoftwareCatalog(filterOf(''), null, new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/software?page=0&size=30');
    });

    it('asks with the keyword, the filters, the page and the platform', async () => {
        const fetchMock = stubFetch(catalogResponse([]));

        await fetchSoftwareCatalog(
            filterOf(`q=elite%202&year=1987&country=${FINLAND}&page=2`, GAMES),
            toPlatformCode('bk'),
            new AbortController().signal,
        );

        expect(fetchMock.mock.calls[0]?.[0]).toBe(
            `/api/software?page=1&size=30&q=elite+2&year=1987&category=${GAMES}&country=${FINLAND}&platform=bk`,
        );
    });

    it('asks with the city, the platforms and the hardware chosen', async () => {
        const fetchMock = stubFetch(catalogResponse([]));

        await fetchSoftwareCatalog(
            filterOf(`country=${FINLAND}&city=${HELSINKI}&platforms=bk,vector&hardware=ay8910,z80`),
            null,
            new AbortController().signal,
        );

        expect(fetchMock.mock.calls[0]?.[0]).toBe(
            `/api/software?page=0&size=30&country=${FINLAND}&city=${HELSINKI}&platforms=bk%2Cvector&hardware=ay8910%2Cz80`,
        );
    });

    it('asks for no platforms of the filter once a platform is chosen in the switch', async () => {
        const fetchMock = stubFetch(catalogResponse([]));

        await fetchSoftwareCatalog(filterOf('platforms=bk&hardware=ay8910'), toPlatformCode('vector'), new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/software?page=0&size=30&platform=vector&hardware=ay8910');
    });

    it('reads the cards, the page and the values of every filter', async () => {
        stubFetch(catalogResponse([{id: ELITE, title: 'Elite', releaseDate: '1987-05-01'}]));

        const catalog = await fetchSoftwareCatalog(filterOf(''), null, new AbortController().signal);

        expect(catalog.page).toMatchObject({number: 0, totalElements: 41, totalPages: 3});
        expect(catalog.page.items).toStrictEqual([{id: ELITE, title: 'Elite', releaseDate: '1987-05-01', screenshots: [], madeBy: []}]);
        expect(catalog.facets).toStrictEqual(FACETS);
    });

    it('fails with a system error on malformed values of a filter', async () => {
        stubFetch(catalogResponse([], {years: [{value: '1987', count: 2}], countries: []}));

        await expect(fetchSoftwareCatalog(filterOf(''), null, new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
    });
});
