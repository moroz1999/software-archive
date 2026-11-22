import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {countriesQuery} from '@/entities/locations';
import {preloadView} from '@/shared/api';

/** Route loader: the countries a city is put in are in its form. */
export function preloadCityNewPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return () => preloadView([queryClient.query(countriesQuery())]);
}
