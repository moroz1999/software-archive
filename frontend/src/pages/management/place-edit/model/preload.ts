import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {countriesQuery, type PlaceKind, placeDetailsQuery, requirePlaceId} from '@/entities/locations';
import {preloadView} from '@/shared/api';

/** Route loader: the country or the city makes the form, a city's with the countries to choose from; a malformed id is a page that does not exist. */
function preloadPlaceEditPage(kind: PlaceKind, queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return ({params}) => {
        const id = requirePlaceId(kind, params['id']);
        const details = queryClient.query(placeDetailsQuery(kind, id));
        return preloadView(kind === 'city' ? [details, queryClient.query(countriesQuery())] : [details]);
    };
}

export function preloadCountryEditPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return preloadPlaceEditPage('country', queryClient);
}

export function preloadCityEditPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return preloadPlaceEditPage('city', queryClient);
}
