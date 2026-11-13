import type {UseQueryResult} from '@tanstack/react-query';
import type {UserEntry} from '@/entities/users';
import type {Page} from '@/shared/api';
import {createRequiredContext} from '@/shared/lib/context';
import type {UserListFilter} from './user-list-filter';

export interface UserListState {
    /** What the address asks the list to show. */
    readonly filter: UserListFilter;
    readonly users: UseQueryResult<Page<UserEntry>>;
    /** Another search or page has been loading over the shown users for a while: the controls are locked. */
    readonly changing: boolean;
    /** Goes to the address of another search or page. */
    readonly show: (filter: UserListFilter) => void;
}

/** The list's filter and query, for the blocks inside a UserListProvider. */
export const [UserListContext, useUserList] = createRequiredContext<UserListState>('UserList');
