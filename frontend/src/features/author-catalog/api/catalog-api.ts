import {toAuthorId} from '@/entities/authors';
import {getJson, mapResponse, type Page, parsePage, readArray, readNullableString, readObject, readString} from '@/shared/api';
import type {AuthorCatalogEntry, City, Country} from '../model/catalog';
import {type AuthorCatalogFilter, type CountryId, toCityId, toCountryId} from '../model/catalog-filter';

/** Entries on one page of the catalog. */
const PAGE_SIZE = 100;

function parseEntry(item: unknown): AuthorCatalogEntry {
    const dto = readObject(item, 'catalog entry');
    return {personId: toAuthorId(readString(dto, 'personId')), name: readString(dto, 'name'), aliasOf: readNullableString(dto, 'aliasOf')};
}

/** A page of the catalog: `GET /api/authors`, with the filters that are set. */
export async function fetchAuthorCatalogPage(filter: AuthorCatalogFilter, signal: AbortSignal): Promise<Page<AuthorCatalogEntry>> {
    const search = new URLSearchParams({page: String(filter.page), size: String(PAGE_SIZE)});
    if (filter.letter !== null) {
        search.set('letter', filter.letter);
    }
    if (filter.country !== null) {
        search.set('country', filter.country);
    }
    if (filter.city !== null) {
        search.set('city', filter.city);
    }
    const json = await getJson(`/api/authors?${search.toString()}`, signal);
    return mapResponse(() => parsePage(json, parseEntry));
}

/** The countries to choose from, by name. */
export async function fetchCountries(signal: AbortSignal): Promise<readonly Country[]> {
    const json = await getJson('/api/countries', signal);
    return mapResponse(() => readArray(json, 'countries').map((item) => {
        const dto = readObject(item, 'country');
        return {id: toCountryId(readString(dto, 'id')), name: readString(dto, 'name')};
    }));
}

/** The cities of a country to choose from, by name. */
export async function fetchCities(country: CountryId, signal: AbortSignal): Promise<readonly City[]> {
    const json = await getJson(`/api/countries/${country}/cities`, signal);
    return mapResponse(() => readArray(json, 'cities').map((item) => {
        const dto = readObject(item, 'city');
        return {id: toCityId(readString(dto, 'id')), name: readString(dto, 'name')};
    }));
}
