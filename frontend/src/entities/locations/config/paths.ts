import type {CityId, CountryId} from '../model/geo';

/** Where the countries and the cities are kept in the app. */
export const GEOGRAPHY_PATH = '/management/geography';
export const COUNTRY_NEW_PATH = '/management/geography/countries/new';
export const CITY_NEW_PATH = '/management/geography/cities/new';
/** The route patterns of the edit pages; their parameter is `id`. */
export const COUNTRY_EDIT_ROUTE = '/management/geography/countries/:id/edit';
export const CITY_EDIT_ROUTE = '/management/geography/cities/:id/edit';

export function countryEditPath(id: CountryId): string {
    return `/management/geography/countries/${id}/edit`;
}

export function cityEditPath(id: CityId): string {
    return `/management/geography/cities/${id}/edit`;
}
