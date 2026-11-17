import {readUserEntry, type UserEntry} from '@/entities/users';
import {getJson, mapResponse, type Page, parsePage} from '@/shared/api';
import type {UserListFilter} from '../model/user-list-filter';

/** Users on one page of the list. */
const PAGE_SIZE = 50;

/** A page of the users: `GET /api/users`, with the search when one is typed. */
export async function fetchUserPage(filter: UserListFilter, signal: AbortSignal): Promise<Page<UserEntry>> {
    const search = new URLSearchParams({page: String(filter.page), size: String(PAGE_SIZE)});
    if (filter.query !== null) {
        search.set('query', filter.query);
    }
    const json = await getJson(`/api/users?${search.toString()}`, signal);
    return mapResponse(() => parsePage(json, readUserEntry));
}
