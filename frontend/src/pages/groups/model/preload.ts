import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs, ShouldRevalidateFunctionArgs} from 'react-router';
import {groupCatalogQuery, parseGroupCatalogFilter} from '@/features/group-catalog';
import {citiesQuery, countriesQuery} from '@/entities/locations';
import {preloadView} from '@/shared/api';

/** Route loader: the catalog of the address and its place filters are at the top of the page. */
export function preloadGroupsPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return ({request}) => {
        const filter = parseGroupCatalogFilter(new URL(request.url).searchParams);
        return preloadView([
            queryClient.query(groupCatalogQuery(filter)),
            queryClient.query(countriesQuery()),
            ...(filter.country === null ? [] : [queryClient.query(citiesQuery(filter.country))]),
        ]);
    };
}

/**
 * Another filter or page of the catalog is shown at once, its entries loading under a spinner, instead of waiting for
 * the preload window: only entering the page preloads it (docs/data-loading.md).
 */
export function shouldPreloadGroupsPage({currentUrl, nextUrl}: ShouldRevalidateFunctionArgs): boolean {
    return currentUrl.pathname !== nextUrl.pathname;
}
