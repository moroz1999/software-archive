import {createRequiredContext} from '@/shared/lib/context';
import type {GroupFormErrors, GroupFormLabel, GroupFormLocation, GroupFormValues} from './group-form';

/** The texts of a refusal under the fields it names. */
export type GroupFormRefusals = Readonly<Partial<Record<keyof GroupFormValues, string>>>;

export interface GroupFormState {
    readonly values: GroupFormValues;
    /** Format errors found on the last submit; a field's error clears once the field is edited. */
    readonly errors: GroupFormErrors;
    /** Why the last submit was refused, under the fields the refusal names; a field's text clears once it is edited. */
    readonly refusals: GroupFormRefusals;
    /** The change handler of a text field. */
    readonly change: (field: 'name' | 'notes') => (value: string) => void;
    readonly changeTypes: (types: readonly string[]) => void;
    readonly changeLabels: (labels: readonly GroupFormLabel[]) => void;
    readonly changeLocations: (locations: readonly GroupFormLocation[]) => void;
    /** Checks the format rules and hands the draft on only when they pass. */
    readonly submit: () => void;
    /** Saving runs: the form is locked. */
    readonly busy: boolean;
    /** Why the last submit was refused, for what no field takes: shown at the top of the form. */
    readonly error: string | undefined;
}

/** The form being filled in, for the fields inside a GroupFormProvider. */
export const [GroupFormContext, useGroupForm] = createRequiredContext<GroupFormState>('GroupForm');
