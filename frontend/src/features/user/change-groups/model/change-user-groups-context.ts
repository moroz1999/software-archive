import type {GrantedGroup, UserEntry} from '@/entities/users';
import {createRequiredContext} from '@/shared/lib/context';

export interface ChangeUserGroupsState {
    readonly user: UserEntry;
    readonly save: (groups: readonly GrantedGroup[]) => void;
    readonly saving: boolean;
    /** Why the groups could not be changed. */
    readonly error: string | undefined;
    /** Forgets the outcome of the last save, as a dialog opened anew. */
    readonly reset: () => void;
}

/** The change of a user's groups, for the controls inside a ChangeUserGroupsProvider. */
export const [ChangeUserGroupsContext, useChangeUserGroups] = createRequiredContext<ChangeUserGroupsState>('ChangeUserGroups');
