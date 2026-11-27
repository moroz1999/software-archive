import {afterEach, describe, expect, it, vi} from 'vitest';
import {json, stubFetch} from '@/shared/lib/test';
import {fetchCities, fetchCountries} from './geo-api';

const COUNTRY = '00000000-0000-0000-0002-000000000001';
const CITY = '00000000-0000-0000-0002-000000000101';

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

        await expect(fetchCities(COUNTRY, new AbortController().signal)).resolves.toStrictEqual([{id: CITY, name: 'Helsinki'}]);
        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/countries/${COUNTRY}/cities`);
    });
});
