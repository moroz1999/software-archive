import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs, ShouldRevalidateFunctionArgs} from 'react-router';
import {categoryTreeQuery} from '@/entities/categories';
import {countriesQuery} from '@/entities/locations';
import {readSelectedPlatform} from '@/entities/platforms';
import {SOFTWARE_LIST_PATH} from '@/entities/software';
import {parseCatalogFilter, softwareCatalogQuery} from '@/features/software/catalog';
import {preloadView} from '@/shared/api';

/**
 * Route loader: the catalog of the address and the chosen platform, the category tree and the countries its filters
 * are titled by are at the top of the page.
 */
export function preloadSoftwarePage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return ({request, params}) => {
        const filter = parseCatalogFilter(params['categoryId'], new URL(request.url).searchParams);
        return preloadView([
            queryClient.query(softwareCatalogQuery(filter, readSelectedPlatform())),
            queryClient.query(categoryTreeQuery()),
            queryClient.query(countriesQuery()),
        ]);
    };
}

function isCatalog(url: URL): boolean {
    return url.pathname === SOFTWARE_LIST_PATH || url.pathname.startsWith(`${SOFTWARE_LIST_PATH}/category/`);
}

/**
 * Another filter, category or page of the catalog is shown at once, its cards loading under a spinner, instead of
 * waiting for the preload window: only entering the catalog preloads it (docs/data-loading.md).
 */
export function shouldPreloadSoftwarePage({currentUrl, nextUrl}: ShouldRevalidateFunctionArgs): boolean {
    return !(isCatalog(currentUrl) && isCatalog(nextUrl));
}
