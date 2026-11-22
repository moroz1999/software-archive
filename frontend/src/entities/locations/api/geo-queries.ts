import {queryOptions, skipToken} from '@tanstack/react-query';
import type {CityId, CountryId} from '../model/geo';
import type {PlaceKind} from '../model/geography';
import {fetchCitiesOfNoCountry, fetchGeoDuplicates, fetchGeography, fetchPlace} from './geography-api';
import {fetchCities, fetchCountries} from './geo-api';

/** The common prefix of every query of countries and cities: a change of one makes them all stale. */
export const GEO_QUERY_KEY = ['geo'] as const;

export function countriesQuery() {
    return queryOptions({
        queryKey: [...GEO_QUERY_KEY, 'countries'],
        queryFn: ({signal}) => fetchCountries(signal),
    });
}

/** The cities of the chosen country; nothing is asked while no country is chosen. */
export function citiesQuery(country: CountryId | null) {
    return queryOptions({
        queryKey: [...GEO_QUERY_KEY, 'cities', country],
        queryFn: country === null ? skipToken : ({signal}) => fetchCities(country, signal),
    });
}

/** Every country with its cities, the tree of the management. */
export function geographyQuery() {
    return queryOptions({
        queryKey: [...GEO_QUERY_KEY, 'geography'],
        queryFn: ({signal}) => fetchGeography(signal),
    });
}

/** One country or city, the block of its form. */
export function placeDetailsQuery(kind: PlaceKind, id: CountryId | CityId) {
    return queryOptions({
        queryKey: [...GEO_QUERY_KEY, kind, id],
        queryFn: ({signal}) => fetchPlace(kind, id, signal),
    });
}

/** The cities of no known country, sorted out in the management. */
export function citiesOfNoCountryQuery() {
    return queryOptions({
        queryKey: [...GEO_QUERY_KEY, 'cities-of-no-country'],
        queryFn: ({signal}) => fetchCitiesOfNoCountry(signal),
    });
}

/** The countries and the cities that may be duplicates. */
export function geoDuplicatesQuery() {
    return queryOptions({
        queryKey: [...GEO_QUERY_KEY, 'duplicates'],
        queryFn: ({signal}) => fetchGeoDuplicates(signal),
    });
}
