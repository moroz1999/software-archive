import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {softwareReleaseQuery} from '../api/software-queries';
import type {ReleaseId} from './release';
import {ReleaseDetailsContext} from './release-details-context';
import type {SoftwareId} from './software';

export interface ReleaseDetailsProviderProps {
    readonly softwareId: SoftwareId;
    readonly releaseId: ReleaseId;
    readonly children: ReactNode;
}

/** Loads a release as it states itself for the blocks inside; takes over the call its route preloaded. */
export function ReleaseDetailsProvider({softwareId, releaseId, children}: ReleaseDetailsProviderProps) {
    const query = useQuery(softwareReleaseQuery(softwareId, releaseId));
    return <ReleaseDetailsContext value={query}>{children}</ReleaseDetailsContext>;
}
