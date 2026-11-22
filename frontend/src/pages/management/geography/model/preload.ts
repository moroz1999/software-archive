import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {citiesOfNoCountryQuery, countriesQuery, geoDuplicatesQuery, geographyQuery} from '@/entities/locations';
import {preloadView} from '@/shared/api';

/** Route loader: the cities of no country, with the countries to put them in, and the possible duplicates over the tree. */
export function preloadManagementGeographyPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return () => preloadView([queryClient.query(citiesOfNoCountryQuery()), queryClient.query(countriesQuery()), queryClient.query(geoDuplicatesQuery()), queryClient.query(geographyQuery())]);
}
