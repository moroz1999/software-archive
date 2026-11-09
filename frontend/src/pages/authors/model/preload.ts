import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs, ShouldRevalidateFunctionArgs} from 'react-router';
import {authorCatalogQuery, citiesQuery, countriesQuery, parseCatalogFilter} from '@/features/author-catalog';
import {preloadView} from '@/shared/api';

/** Route loader: the catalog of the address and its place filters are at the top of the page. */
export function preloadAuthorsPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return ({request}) => {
        const filter = parseCatalogFilter(new URL(request.url).searchParams);
        return preloadView([
            queryClient.query(authorCatalogQuery(filter)),
            queryClient.query(countriesQuery()),
            ...(filter.country === null ? [] : [queryClient.query(citiesQuery(filter.country))]),
        ]);
    };
}

/**
 * Another filter or page of the catalog is shown at once, its entries loading under a spinner, instead of waiting for
 * the preload window: only entering the page preloads it (docs/data-loading.md).
 */
export function shouldPreloadAuthorsPage({currentUrl, nextUrl}: ShouldRevalidateFunctionArgs): boolean {
    return currentUrl.pathname !== nextUrl.pathname;
}
