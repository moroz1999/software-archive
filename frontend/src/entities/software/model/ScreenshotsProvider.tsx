import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {screenshotsQuery} from '../api/software-queries';
import type {Gallery} from './screenshot';
import {ScreenshotsContext} from './screenshots-context';

export interface ScreenshotsProviderProps {
    readonly softwareId: string;
    /** The release whose screenshots these are; none for the software's own. */
    readonly releaseId?: string | null;
    /** The gallery of the software these pictures are of; screenshots by default. */
    readonly gallery?: Gallery;
    /** The block has come into view: only then are the screenshots asked for (docs/data-loading.md). */
    readonly enabled: boolean;
    readonly children: ReactNode;
}

/** Loads the screenshots of software, or of its release, for the blocks inside. */
export function ScreenshotsProvider({softwareId, releaseId = null, gallery = 'screenshots', enabled, children}: ScreenshotsProviderProps) {
    const query = useQuery({...screenshotsQuery(softwareId, releaseId, gallery), enabled});
    return <ScreenshotsContext value={query}>{children}</ScreenshotsContext>;
}
