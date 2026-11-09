import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {groupDetailsQuery} from '../api/group-queries';
import type {GroupId} from './group';
import {GroupDetailsContext} from './group-details-context';

export interface GroupDetailsProviderProps {
    readonly id: GroupId;
    readonly children: ReactNode;
}

/** Loads one group's details for the blocks inside; takes over the call its route preloaded. */
export function GroupDetailsProvider({id, children}: GroupDetailsProviderProps) {
    const query = useQuery(groupDetailsQuery(id));
    return <GroupDetailsContext value={query}>{children}</GroupDetailsContext>;
}
