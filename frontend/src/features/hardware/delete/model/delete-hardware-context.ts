import type {HardwareDetails} from '@/entities/hardware';
import {createRequiredContext} from '@/shared/lib/context';

export interface DeleteHardwareState {
    readonly hardware: HardwareDetails;
    readonly remove: () => void;
    readonly deleting: boolean;
    readonly deleted: boolean;
    /** Why the group or the item could not be deleted. */
    readonly error: string | undefined;
}

/** The deletion, for the controls inside a DeleteHardwareProvider. */
export const [DeleteHardwareContext, useDeleteHardware] = createRequiredContext<DeleteHardwareState>('DeleteHardware');
