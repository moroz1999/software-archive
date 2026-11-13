import type {EmailProblem, PasswordProblem, UserNameProblem} from '@/entities/users';
import type {FieldRefusals} from '@/shared/api';
import {createRequiredContext} from '@/shared/lib/context';

export interface RegisterValues {
    readonly name: string;
    readonly email: string;
    readonly password: string;
    /** The password once more, to catch a typo. */
    readonly repeat: string;
}

export interface RegisterErrors {
    readonly name?: UserNameProblem;
    readonly email?: EmailProblem;
    readonly password?: PasswordProblem;
    readonly repeat?: 'differs';
}

export interface RegisterState {
    readonly values: RegisterValues;
    /** Format errors found on the last submit; a field's error clears once the field is edited. */
    readonly errors: RegisterErrors;
    /** Why the last submit was refused, under the fields the refusal names; a field's text clears once it is edited. */
    readonly refusals: FieldRefusals;
    readonly change: (field: keyof RegisterValues) => (value: string) => void;
    /** Checks the format rules and registers only when they pass. */
    readonly submit: () => void;
    /** Registering runs: the form is locked. */
    readonly busy: boolean;
    /** Why the last submit was refused, for what no field takes. */
    readonly error: string | undefined;
    /** The email the confirmation link went to, once registered. */
    readonly registered: string | null;
}

/** The registration being filled in, for the form inside a RegisterProvider. */
export const [RegisterContext, useRegister] = createRequiredContext<RegisterState>('Register');
