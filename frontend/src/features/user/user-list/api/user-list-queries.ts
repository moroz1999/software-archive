import {keepPreviousData, queryOptions} from '@tanstack/react-query';
import {USERS_QUERY_KEY} from '@/entities/users';
import type {UserListFilter} from '../model/user-list-filter';
import {fetchUserPage} from './user-list-api';

/** A page of the users of a search; another search or page keeps the shown one until its own arrives. */
export function userListQuery(filter: UserListFilter) {
    return queryOptions({
        queryKey: [...USERS_QUERY_KEY, 'list', filter],
        queryFn: ({signal}) => fetchUserPage(filter, signal),
        placeholderData: keepPreviousData,
    });
}
