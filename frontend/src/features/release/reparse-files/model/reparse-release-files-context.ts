import {createRequiredContext} from '@/shared/lib/context';

export interface ReparseReleaseFilesState {
    readonly reparse: () => void;
    readonly reparsing: boolean;
    /** Why the file could not be parsed again. */
    readonly error: string | undefined;
}

/** Parsing a release file again, for the controls inside a ReparseReleaseFilesProvider. */
export const [ReparseReleaseFilesContext, useReparseReleaseFiles] =
    createRequiredContext<ReparseReleaseFilesState>('ReparseReleaseFiles');
