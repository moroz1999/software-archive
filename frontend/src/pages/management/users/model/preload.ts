import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs, ShouldRevalidateFunctionArgs} from 'react-router';
import {parseUserListFilter, userListQuery} from '@/features/user/user-list';
import {preloadView} from '@/shared/api';

/** Route loader: the users of the search in the address are at the top of the page. */
export function preloadManagementUsersPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return ({request}) => preloadView([queryClient.query(userListQuery(parseUserListFilter(new URL(request.url).searchParams)))]);
}

/**
 * Another search or page of the list is shown at once, its users loading under a spinner, instead of waiting for
 * the preload window: only entering the page preloads it (docs/data-loading.md).
 */
export function shouldPreloadManagementUsersPage({currentUrl, nextUrl}: ShouldRevalidateFunctionArgs): boolean {
    return currentUrl.pathname !== nextUrl.pathname;
}
