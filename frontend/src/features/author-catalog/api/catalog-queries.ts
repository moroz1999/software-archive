import {keepPreviousData, queryOptions, skipToken} from '@tanstack/react-query';
import {AUTHOR_QUERY_KEY} from '@/entities/authors';
import type {AuthorCatalogFilter, CountryId} from '../model/catalog-filter';
import {fetchAuthorCatalogPage, fetchCities, fetchCountries} from './catalog-api';

/** The common prefix of the countries and cities to choose from. */
const GEO_QUERY_KEY = ['geo'] as const;

/** A page of the catalog; another filter or page keeps the shown one until its own arrives. */
export function authorCatalogQuery(filter: AuthorCatalogFilter) {
    return queryOptions({
        queryKey: [...AUTHOR_QUERY_KEY, 'catalog', filter],
        queryFn: ({signal}) => fetchAuthorCatalogPage(filter, signal),
        placeholderData: keepPreviousData,
    });
}

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
