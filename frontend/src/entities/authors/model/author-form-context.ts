import {createRequiredContext} from '@/shared/lib/context';
import type {AuthorFormErrors, AuthorFormLabel, AuthorFormLocation, AuthorFormValues} from './author-form';

/** The texts of a refusal under the fields it names. */
export type AuthorFormRefusals = Readonly<Partial<Record<keyof AuthorFormValues, string>>>;

export interface AuthorFormState {
    readonly values: AuthorFormValues;
    /** Format errors found on the last submit; a field's error clears once the field is edited. */
    readonly errors: AuthorFormErrors;
    /** Why the last submit was refused, under the fields the refusal names; a field's text clears once it is edited. */
    readonly refusals: AuthorFormRefusals;
    /** The change handler of a text field. */
    readonly change: (field: 'realName' | 'notes') => (value: string) => void;
    readonly changeLabels: (labels: readonly AuthorFormLabel[]) => void;
    readonly changeLocations: (locations: readonly AuthorFormLocation[]) => void;
    /** Checks the format rules and hands the draft on only when they pass. */
    readonly submit: () => void;
    /** Saving runs: the form is locked. */
    readonly busy: boolean;
    /** Why the last submit was refused, for what no field takes: shown at the top of the form. */
    readonly error: string | undefined;
}

/** The form being filled in, for the fields inside an AuthorFormProvider. */
export const [AuthorFormContext, useAuthorForm] = createRequiredContext<AuthorFormState>('AuthorForm');
