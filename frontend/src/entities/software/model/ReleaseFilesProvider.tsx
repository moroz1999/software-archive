import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {releaseFilesQuery} from '../api/software-queries';
import type {ReleaseId} from './release';
import {ReleaseFilesContext} from './release-files-context';
import type {SoftwareId} from './software';

export interface ReleaseFilesProviderProps {
    readonly softwareId: SoftwareId;
    readonly releaseId: ReleaseId;
    readonly enabled: boolean;
    readonly children: ReactNode;
}

/** Loads the parsed file tree of one release: null while its file waits for parsing. */
export function ReleaseFilesProvider({softwareId, releaseId, enabled, children}: ReleaseFilesProviderProps) {
    const query = useQuery({...releaseFilesQuery(softwareId, releaseId), enabled});
    return <ReleaseFilesContext value={query}>{children}</ReleaseFilesContext>;
}
