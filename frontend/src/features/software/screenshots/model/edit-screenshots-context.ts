import type {UseQueryResult} from '@tanstack/react-query';
import type {Gallery, Screenshot, ScreenshotId} from '@/entities/software';
import {createRequiredContext} from '@/shared/lib/context';

/** The screenshots being changed and the changes to make (docs/domain/software.md#screenshots). */
export interface EditScreenshotsState {
    /** The gallery the pictures are of, which names them. */
    readonly gallery: Gallery;
    readonly query: UseQueryResult<readonly Screenshot[]>;
    /** Uploads the files after the screenshots there are; files that cannot be screenshots are refused before sending. */
    readonly upload: (files: readonly File[]) => void;
    /** Moves a screenshot a place back (-1) or forth (1). */
    readonly move: (id: ScreenshotId, by: -1 | 1) => void;
    readonly remove: (id: ScreenshotId) => void;
    /** A change is running: the controls are locked. */
    readonly busy: boolean;
    /** Why the last upload or reordering failed, shown next to the screenshots. */
    readonly error: string | undefined;
    /** Why the last deletion failed, shown in its confirmation. */
    readonly deleteError: string | undefined;
    /** The screenshot whose deletion is done, so its confirmation closes. */
    readonly deleted: ScreenshotId | undefined;
}

export const [EditScreenshotsContext, useEditScreenshots] = createRequiredContext<EditScreenshotsState>('EditScreenshots');
