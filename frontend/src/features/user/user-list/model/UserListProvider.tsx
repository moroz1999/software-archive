import {type ReactNode, useMemo} from 'react';
import {useQuery} from '@tanstack/react-query';
import {useNavigate, useSearchParams} from 'react-router';
import {useLoadingLock} from '@/shared/lib/loading';
import {userListQuery} from '../api/user-list-queries';
import {UserListContext} from './user-list-context';
import {parseUserListFilter, type UserListFilter, userListHref} from './user-list-filter';

/**
 * Holds the list's filter, read from the address, and its query; takes over the call its route preloaded. A search
 * is changed by going to its address, so it can be shared and the back button works.
 */
export function UserListProvider({children}: {readonly children: ReactNode}) {
    const [search] = useSearchParams();
    const navigate = useNavigate();
    const filter = useMemo(() => parseUserListFilter(search), [search]);
    const users = useQuery(userListQuery(filter));
    const changing = useLoadingLock(users.isPlaceholderData || users.isRefetching);

    const state = useMemo(() => ({
        filter,
        users,
        changing,
        show: (next: UserListFilter) => {
            void navigate(userListHref(next));
        },
    }), [filter, users, changing, navigate]);

    return <UserListContext value={state}>{children}</UserListContext>;
}
