import {type ReactNode, useState} from 'react';
import {useMutation} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {emailProblem, passwordProblem, userNameProblem} from '@/entities/users';
import {businessErrorOf, splitRefusal, unedited} from '@/shared/api';
import {parseUiLanguage} from '@/shared/lib/i18n';
import {register} from '../api/register-api';
import {registerMessages} from '../config/messages';
import {RegisterContext, type RegisterErrors, type RegisterState, type RegisterValues} from './register-context';

const EMPTY: RegisterValues = {name: '', email: '', password: '', repeat: ''};

function validate(values: RegisterValues): RegisterErrors {
    const name = userNameProblem(values.name);
    const email = emailProblem(values.email);
    const password = passwordProblem(values.password);
    return {
        ...(name === null ? {} : {name}),
        ...(email === null ? {} : {email}),
        ...(password === null ? {} : {password}),
        ...(values.repeat === values.password ? {} : {repeat: 'differs'}),
    };
}

function withoutError(errors: RegisterErrors, field: keyof RegisterErrors): RegisterErrors {
    const {name, email, password, repeat} = errors;
    return {
        ...(field === 'name' || name === undefined ? {} : {name}),
        ...(field === 'email' || email === undefined ? {} : {email}),
        ...(field === 'password' || password === undefined ? {} : {password}),
        ...(field === 'repeat' || repeat === undefined ? {} : {repeat}),
    };
}

/** Registering an account: the letter with the confirmation link goes out in the language of the interface. */
export function RegisterProvider({children}: {readonly children: ReactNode}) {
    const {t, i18n} = useTranslation();
    const [values, setValues] = useState<RegisterValues>(EMPTY);
    const [errors, setErrors] = useState<RegisterErrors>({});
    const [edited, setEdited] = useState<ReadonlySet<string>>(new Set());
    const {mutate, isPending, isSuccess, variables, error} = useMutation({
        mutationFn: register,
        onMutate: () => {
            setEdited(new Set());
        },
    });
    const refusal = splitRefusal(businessErrorOf(error), registerMessages(t), ['name', 'email']);

    const state: RegisterState = {
        values,
        errors,
        refusals: unedited(refusal.fields, edited),
        change: (field) => (value) => {
            setValues((current) => ({...current, [field]: value}));
            setErrors((current) => withoutError(current, field));
            setEdited((current) => new Set([...current, field]));
        },
        submit: () => {
            const found = validate(values);
            setErrors(found);
            if (Object.keys(found).length === 0) {
                mutate({
                    name: values.name,
                    email: values.email.trim(),
                    password: values.password,
                    language: parseUiLanguage(i18n.resolvedLanguage) ?? 'en',
                });
            }
        },
        busy: isPending,
        error: refusal.form,
        registered: isSuccess ? variables.email : null,
    };

    return <RegisterContext value={state}>{children}</RegisterContext>;
}
