import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs, ShouldRevalidateFunctionArgs} from 'react-router';
import {readSelectedPlatform} from '@/entities/platforms';
import {fullSearchQuery, parseFullSearchFilter, SEARCH_PATH} from '@/features/search';
import {preloadView} from '@/shared/api';

/** Route loader: the page of the full search of the address, of the chosen platform; nothing without a keyword. */
export function preloadSearchPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return ({request}) => {
        const filter = parseFullSearchFilter(new URL(request.url).searchParams);
        if (filter.keyword === null) {
            return Promise.resolve(null);
        }
        return preloadView([queryClient.query(fullSearchQuery(filter, readSelectedPlatform()))]);
    };
}

/**
 * Another page or choice of kinds is shown at once, its matches loading under a spinner, instead of waiting for the
 * preload window: only entering the search preloads it (docs/data-loading.md).
 */
export function shouldPreloadSearchPage({currentUrl, nextUrl}: ShouldRevalidateFunctionArgs): boolean {
    return !(currentUrl.pathname === SEARCH_PATH && nextUrl.pathname === SEARCH_PATH);
}
