import type {SyntheticEvent} from 'react';
import {useTranslation} from 'react-i18next';
import {EMAIL_MAX_LENGTH, EMAIL_PROBLEM_KEYS, PASSWORD_PROBLEM_KEYS, USER_NAME_MAX_LENGTH, USER_NAME_PROBLEM_KEYS} from '@/entities/users';
import {Button} from '@/shared/ui/button';
import {Field} from '@/shared/ui/field';
import {Inline} from '@/shared/ui/inline';
import {Input} from '@/shared/ui/input';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {useRegister} from '../model/register-context';
import {RegisterProvider} from '../model/RegisterProvider';

/** The registration form; once sent, it tells where the confirmation link went. */
export function RegisterForm() {
    return (
        <RegisterProvider>
            <Registration/>
        </RegisterProvider>
    );
}

function Registration() {
    const {t} = useTranslation();
    const {values, errors, refusals, change, submit, busy, error, registered} = useRegister();

    if (registered !== null) {
        return <Text role="status">{t('account.registered', {email: registered})}</Text>;
    }

    function onSubmit(event: SyntheticEvent) {
        event.preventDefault();
        submit();
    }

    return (
        <form noValidate onSubmit={onSubmit}>
            <Stack gap="lg">
                {error !== undefined && <Text role="alert" tone="danger">{error}</Text>}
                <Field label={t('account.name')} error={errors.name === undefined ? refusals['name'] : t(USER_NAME_PROBLEM_KEYS[errors.name])}>
                    {(control) => (
                        <Input {...control} autoComplete="username" maxLength={USER_NAME_MAX_LENGTH} value={values.name} onChange={change('name')} disabled={busy}/>
                    )}
                </Field>
                <Field label={t('account.email')} error={errors.email === undefined ? refusals['email'] : t(EMAIL_PROBLEM_KEYS[errors.email])}>
                    {(control) => (
                        <Input {...control} type="email" autoComplete="email" maxLength={EMAIL_MAX_LENGTH} value={values.email} onChange={change('email')} disabled={busy}/>
                    )}
                </Field>
                <Field label={t('account.password')} error={errors.password === undefined ? undefined : t(PASSWORD_PROBLEM_KEYS[errors.password])}>
                    {(control) => (
                        <Input {...control} type="password" autoComplete="new-password" value={values.password} onChange={change('password')} disabled={busy}/>
                    )}
                </Field>
                <Field label={t('account.repeatPassword')} error={errors.repeat === undefined ? undefined : t('validation.passwordsDiffer')}>
                    {(control) => (
                        <Input {...control} type="password" autoComplete="new-password" value={values.repeat} onChange={change('repeat')} disabled={busy}/>
                    )}
                </Field>
                <Inline justify="end">
                    <Button type="submit" loading={busy}>{t('account.submitRegistration')}</Button>
                </Inline>
            </Stack>
        </form>
    );
}
