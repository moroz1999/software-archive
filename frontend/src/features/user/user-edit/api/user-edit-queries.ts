import {queryOptions} from '@tanstack/react-query';
import {USERS_QUERY_KEY} from '@/entities/users';
import {fetchUser} from './user-edit-api';

/** One account to edit; a change of any user makes it stale with the lists. */
export function userEditQuery(id: string) {
    return queryOptions({
        queryKey: [...USERS_QUERY_KEY, 'details', id],
        queryFn: ({signal}) => fetchUser(id, signal),
    });
}
