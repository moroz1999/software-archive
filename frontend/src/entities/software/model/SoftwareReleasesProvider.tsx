import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {softwareReleasesQuery} from '../api/software-queries';
import type {SoftwareId} from './software';
import {SoftwareReleasesContext} from './software-releases-context';

export interface SoftwareReleasesProviderProps {
    readonly id: SoftwareId;
    /** The block has come into view: only then are the releases asked for (docs/data-loading.md). */
    readonly enabled: boolean;
    readonly children: ReactNode;
}

/** Loads the releases of one software for the blocks inside. */
export function SoftwareReleasesProvider({id, enabled, children}: SoftwareReleasesProviderProps) {
    const query = useQuery({...softwareReleasesQuery(id), enabled});
    return <SoftwareReleasesContext value={query}>{children}</SoftwareReleasesContext>;
}
