import type {PasswordProblem} from '@/entities/users';
import {createRequiredContext} from '@/shared/lib/context';

export interface SignInValues {
    /** The user name or the email. */
    readonly login: string;
    readonly password: string;
    readonly remember: boolean;
}

export interface SignInErrors {
    readonly login?: 'required';
    readonly password?: PasswordProblem;
}

export interface SignInState {
    readonly values: SignInValues;
    /** Format errors found on the last submit; a field's error clears once the field is edited. */
    readonly errors: SignInErrors;
    readonly change: (field: 'login' | 'password') => (value: string) => void;
    readonly changeRemember: (remember: boolean) => void;
    /** Checks the format rules and signs in only when they pass. */
    readonly submit: () => void;
    /** Signing in runs: the form is locked. */
    readonly busy: boolean;
    /** Why signing in was refused. */
    readonly error: string | undefined;
    /** The account is not confirmed yet: a new link can be sent with what was typed. */
    readonly unconfirmed: boolean;
    readonly resend: () => void;
    readonly resending: boolean;
    /** A new link went out. */
    readonly resent: boolean;
    readonly signOut: () => void;
    readonly signingOut: boolean;
}

/** Signing in and out, for the account window inside a SignInProvider. */
export const [SignInContext, useSignIn] = createRequiredContext<SignInState>('SignIn');
