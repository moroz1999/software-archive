import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {releaseFilesQuery} from '../api/software-queries';
import {ReleaseFilesContext} from './release-files-context';

export interface ReleaseFilesProviderProps {
    readonly softwareId: string;
    readonly releaseId: string;
    readonly enabled: boolean;
    readonly children: ReactNode;
}

/** Loads the parsed file tree of one release: null while its file waits for parsing. */
export function ReleaseFilesProvider({softwareId, releaseId, enabled, children}: ReleaseFilesProviderProps) {
    const query = useQuery({...releaseFilesQuery(softwareId, releaseId), enabled});
    return <ReleaseFilesContext value={query}>{children}</ReleaseFilesContext>;
}
