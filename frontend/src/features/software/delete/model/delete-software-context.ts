import type {Software} from '@/entities/software';
import {createRequiredContext} from '@/shared/lib/context';

export interface DeleteSoftwareState {
    readonly software: Software;
    readonly remove: () => void;
    readonly deleting: boolean;
    readonly deleted: boolean;
    /** Why the software could not be deleted. */
    readonly error: string | undefined;
}

/** The deletion, for the controls inside a DeleteSoftwareProvider. */
export const [DeleteSoftwareContext, useDeleteSoftware] = createRequiredContext<DeleteSoftwareState>('DeleteSoftware');
