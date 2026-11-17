import type {GrantedGroup, UserEntry} from '@/entities/users';
import type {FieldRefusals} from '@/shared/api';
import {createRequiredContext} from '@/shared/lib/context';
import type {UserFormErrors, UserFormValues} from './user-form';

export interface EditUserState {
    /** The account being edited, as it was loaded. */
    readonly user: UserEntry;
    readonly values: UserFormValues;
    /** Format errors found on the last submit; a field's error clears once the field is edited. */
    readonly errors: UserFormErrors;
    /** Why the last submit was refused, under the fields the refusal names; a field's text clears once it is edited. */
    readonly refusals: FieldRefusals;
    readonly change: (field: 'name' | 'email') => (value: string) => void;
    readonly toggleGroup: (group: GrantedGroup, on: boolean) => void;
    /** Checks the format rules and saves only when they pass. */
    readonly submit: () => void;
    /** Saving runs: the form is locked. */
    readonly busy: boolean;
    /** Why the last submit was refused, for what no field takes: shown at the top of the form. */
    readonly error: string | undefined;
}

/** The account being edited, for the form inside an EditUserProvider. */
export const [EditUserContext, useEditUser] = createRequiredContext<EditUserState>('EditUser');
