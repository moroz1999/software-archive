import {queryOptions} from '@tanstack/react-query';
import {type UserId, USERS_QUERY_KEY} from '@/entities/users';
import {fetchUser} from './user-edit-api';

/** One account to edit; a change of any user makes it stale with the lists. */
export function userEditQuery(id: UserId) {
    return queryOptions({
        queryKey: [...USERS_QUERY_KEY, 'details', id],
        queryFn: ({signal}) => fetchUser(id, signal),
    });
}
