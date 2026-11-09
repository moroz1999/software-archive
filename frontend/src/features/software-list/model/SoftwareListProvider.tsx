import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {usePlatformSelection} from '@/entities/platforms';
import {softwareListQuery} from '../api/software-list-query';
import {SoftwareListContext} from './software-list-context';

/**
 * Loads the first page of the archive's software, of the chosen platform, for the blocks inside; takes over the call
 * its route preloaded.
 */
export function SoftwareListProvider({children}: {readonly children: ReactNode}) {
    const query = useQuery(softwareListQuery(usePlatformSelection().platform));
    return <SoftwareListContext value={query}>{children}</SoftwareListContext>;
}
