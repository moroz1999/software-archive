import {afterEach, describe, expect, it, vi} from 'vitest';
import {SystemError} from '@/shared/api';
import {json, pageResponse, stubFetch} from '@/shared/lib/test';
import {parseCatalogFilter, toCountryId} from '../model/catalog-filter';
import {fetchAuthorCatalogPage, fetchCities, fetchCountries} from './catalog-api';

const PERSON = '00000000-0000-0000-0003-000000000001';
const COUNTRY = '00000000-0000-0000-0002-000000000001';
const CITY = '00000000-0000-0000-0002-000000000101';

describe(fetchAuthorCatalogPage, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('asks for a page of 100 entries without filters', async () => {
        const fetchMock = stubFetch(json(pageResponse([
            {personId: PERSON, name: 'Axe', realName: 'John Doe', aliasOf: 'Zed'},
            {personId: PERSON, name: 'Zed', realName: null, aliasOf: null},
        ], {size: 100})));

        const page = await fetchAuthorCatalogPage(parseCatalogFilter(new URLSearchParams()), new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/authors?page=0&size=100');
        expect(page.items).toStrictEqual([
            {personId: PERSON, name: 'Axe', realName: 'John Doe', aliasOf: 'Zed'},
            {personId: PERSON, name: 'Zed', realName: null, aliasOf: null},
        ]);
    });

    it('asks with the filters and the page', async () => {
        const fetchMock = stubFetch(json(pageResponse([])));

        await fetchAuthorCatalogPage(parseCatalogFilter(new URLSearchParams(`letter=%23&country=${COUNTRY}&city=${CITY}&page=2`)), new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/authors?page=1&size=100&letter=%23&country=${COUNTRY}&city=${CITY}`);
    });

    it('fails with a system error on a malformed answer', async () => {
        stubFetch(json(pageResponse([{personId: 'nope', name: 'Axe', realName: null, aliasOf: null}])));

        await expect(fetchAuthorCatalogPage(parseCatalogFilter(new URLSearchParams()), new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
    });
});

describe(fetchCountries, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('reads the countries', async () => {
        const fetchMock = stubFetch(json([{id: COUNTRY, name: 'Finland'}]));

        await expect(fetchCountries(new AbortController().signal)).resolves.toStrictEqual([{id: COUNTRY, name: 'Finland'}]);
        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/countries');
    });
});

describe(fetchCities, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('reads the cities of a country', async () => {
        const fetchMock = stubFetch(json([{id: CITY, name: 'Helsinki'}]));

        await expect(fetchCities(toCountryId(COUNTRY), new AbortController().signal)).resolves.toStrictEqual([{id: CITY, name: 'Helsinki'}]);
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/countries/${COUNTRY}/cities`);
    });

    it('fails with a system error on a malformed answer', async () => {
        stubFetch(json({id: CITY}));

        await expect(fetchCities(toCountryId(COUNTRY), new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
    });
});
