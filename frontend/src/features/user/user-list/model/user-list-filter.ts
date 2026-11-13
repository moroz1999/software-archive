import {MANAGEMENT_USERS_PATH} from '@/entities/users';
import {pageParam, parsePageParam} from '@/shared/lib/page-param';

/** What the list of users shows: the search typed and its page, counted from 0. */
export interface UserListFilter {
    readonly query: string | null;
    readonly page: number;
}

/** The filter in the address of the list; a search of nothing is none. */
export function parseUserListFilter(search: URLSearchParams): UserListFilter {
    const query = search.get('query')?.trim() ?? '';
    return {query: query === '' ? null : query, page: parsePageParam(search.get('page'))};
}

/** The address of the list showing the filter; no search and the first page are left out. */
export function userListHref(filter: UserListFilter): string {
    const search = new URLSearchParams();
    for (const [key, value] of [['query', filter.query], ['page', pageParam(filter.page)]] as const) {
        if (value !== null) {
            search.set(key, value);
        }
    }
    const query = search.toString();
    return query === '' ? MANAGEMENT_USERS_PATH : `${MANAGEMENT_USERS_PATH}?${query}`;
}
