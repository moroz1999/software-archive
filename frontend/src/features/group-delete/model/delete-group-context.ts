import type {GroupId} from '@/entities/groups';
import {createRequiredContext} from '@/shared/lib/context';

/** The group to delete, under the name it is shown by. */
export interface DeletedGroup {
    readonly id: GroupId;
    readonly name: string;
}

export interface DeleteGroupState {
    readonly group: DeletedGroup;
    readonly remove: () => void;
    readonly deleting: boolean;
    readonly deleted: boolean;
    /** Why the group could not be deleted. */
    readonly error: string | undefined;
}

/** The deletion, for the controls inside a DeleteGroupProvider. */
export const [DeleteGroupContext, useDeleteGroup] = createRequiredContext<DeleteGroupState>('DeleteGroup');
