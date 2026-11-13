import {type ReactNode, useState} from 'react';
import {useMutation} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {passwordProblem} from '@/entities/users';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {parseUiLanguage} from '@/shared/lib/i18n';
import {logIn, logOut, resendConfirmation} from '../api/sign-in-api';
import {signInMessages, UNCONFIRMED} from '../config/messages';
import {SignInContext, type SignInErrors, type SignInState, type SignInValues} from './sign-in-context';

function validate(values: SignInValues): SignInErrors {
    const password = passwordProblem(values.password);
    return {
        ...(values.login.trim() === '' ? {login: 'required'} : {}),
        ...(password === null ? {} : {password}),
    };
}

function withoutError(errors: SignInErrors, field: keyof SignInErrors): SignInErrors {
    const {login, password} = errors;
    return {
        ...(field === 'login' || login === undefined ? {} : {login}),
        ...(field === 'password' || password === undefined ? {} : {password}),
    };
}

/**
 * Signing in and out, through the session of the browser, which then tells who is signed in; an unconfirmed user may
 * ask for a new link with what they typed.
 */
export function SignInProvider({children}: {readonly children: ReactNode}) {
    const {t, i18n} = useTranslation();
    const [values, setValues] = useState<SignInValues>({login: '', password: '', remember: true});
    const [errors, setErrors] = useState<SignInErrors>({});

    const signIn = useMutation({
        mutationFn: logIn,
        onSuccess: () => {
            setValues({login: '', password: '', remember: true});
        },
    });
    const resend = useMutation({mutationFn: resendConfirmation});
    const signOut = useMutation({
        mutationFn: logOut,
        onSuccess: () => {
            signIn.reset();
            resend.reset();
        },
    });
    const refusal = businessErrorOf(signIn.error);

    const state: SignInState = {
        values,
        errors,
        change: (field) => (value) => {
            setValues((current) => ({...current, [field]: value}));
            setErrors((current) => withoutError(current, field));
        },
        changeRemember: (remember) => {
            setValues((current) => ({...current, remember}));
        },
        submit: () => {
            const found = validate(values);
            setErrors(found);
            if (Object.keys(found).length === 0) {
                resend.reset();
                signIn.mutate(values);
            }
        },
        busy: signIn.isPending,
        error: refusal === undefined ? undefined : businessErrorMessage(refusal, signInMessages(t)),
        unconfirmed: refusal?.code === UNCONFIRMED,
        resend: () => {
            resend.mutate({login: values.login, password: values.password, language: parseUiLanguage(i18n.resolvedLanguage) ?? 'en'});
        },
        resending: resend.isPending,
        resent: resend.isSuccess,
        signOut: () => {
            signOut.mutate();
        },
        signingOut: signOut.isPending,
    };

    return <SignInContext value={state}>{children}</SignInContext>;
}
