import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import type {UserId} from '@/entities/users';
import {userEditQuery} from '../api/user-edit-queries';
import {UserDetailsContext} from './user-details-context';

export interface UserDetailsProviderProps {
    readonly id: UserId;
    readonly children: ReactNode;
}

/** Loads the account to edit for the blocks inside; takes over the call its route preloaded. */
export function UserDetailsProvider({id, children}: UserDetailsProviderProps) {
    const query = useQuery(userEditQuery(id));
    return <UserDetailsContext value={query}>{children}</UserDetailsContext>;
}
