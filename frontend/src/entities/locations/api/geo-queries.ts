import {queryOptions, skipToken} from '@tanstack/react-query';
import type {CountryId} from '../model/geo';
import {fetchCities, fetchCountries} from './geo-api';

/** The common prefix of the countries and cities to choose from. */
const GEO_QUERY_KEY = ['geo'] as const;

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
