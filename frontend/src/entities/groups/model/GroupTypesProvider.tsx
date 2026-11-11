import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {groupTypesQuery} from '../api/group-queries';
import {GroupTypesContext} from './group-types-context';

/** Loads the group types to choose from for the blocks inside. */
export function GroupTypesProvider({children}: {readonly children: ReactNode}) {
    const query = useQuery(groupTypesQuery());
    return <GroupTypesContext value={query}>{children}</GroupTypesContext>;
}
