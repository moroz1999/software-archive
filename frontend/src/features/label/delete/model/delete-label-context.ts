import type {LabelId} from '@/entities/labels';
import {createRequiredContext} from '@/shared/lib/context';

/** The label to delete, under its name. */
export interface DeletedLabel {
    readonly id: LabelId;
    readonly name: string;
}

export interface DeleteLabelState {
    readonly label: DeletedLabel;
    readonly remove: () => void;
    readonly deleting: boolean;
    readonly deleted: boolean;
    /** Why the label could not be deleted. */
    readonly error: string | undefined;
}

/** The deletion, for the controls inside a DeleteLabelProvider. */
export const [DeleteLabelContext, useDeleteLabel] = createRequiredContext<DeleteLabelState>('DeleteLabel');
