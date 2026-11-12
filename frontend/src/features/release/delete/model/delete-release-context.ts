import {createRequiredContext} from '@/shared/lib/context';

export interface DeleteReleaseState {
    /** How the release is named in the confirmation. */
    readonly name: string;
    readonly remove: () => void;
    readonly deleting: boolean;
    readonly deleted: boolean;
    /** Why the release could not be deleted. */
    readonly error: string | undefined;
}

/** The deletion, for the controls inside a DeleteReleaseProvider. */
export const [DeleteReleaseContext, useDeleteRelease] = createRequiredContext<DeleteReleaseState>('DeleteRelease');
