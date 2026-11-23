import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {hardwareGroupsQuery} from '../api/hardware-queries';
import {HardwareGroupsContext} from './hardware-context';

/** Loads every group with its hardware for the blocks inside; takes over the call its route preloaded. */
export function HardwareGroupsProvider({children}: {readonly children: ReactNode}) {
    const query = useQuery(hardwareGroupsQuery());
    return <HardwareGroupsContext value={query}>{children}</HardwareGroupsContext>;
}
