import {api, apiError, type components} from '@/shared/api';
import type {UserListFilter} from '../model/user-list-filter';

/** Users on one page of the list. */
const PAGE_SIZE = 50;

/** A page of the users: `GET /api/users`, with the search when one is typed. */
export async function fetchUserPage(filter: UserListFilter, signal: AbortSignal): Promise<components['schemas']['PagedModelUserEntryDto']> {
    const {data, error, response} = await api.GET('/api/users', {params: {query: {page: filter.page, size: PAGE_SIZE, query: filter.query}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}
