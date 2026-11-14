import {useTranslation} from 'react-i18next';
import {EMAIL_MAX_LENGTH, EMAIL_PROBLEM_KEYS, PASSWORD_PROBLEM_KEYS, USER_NAME_MAX_LENGTH, USER_NAME_PROBLEM_KEYS} from '@/entities/users';
import {Button} from '@/shared/ui/button';
import {Form, FormActions, FormField, FormMessage} from '@/shared/ui/form';
import {Input} from '@/shared/ui/input';
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

    return (
        <Form onSubmit={submit}>
            {error !== undefined && <FormMessage>{error}</FormMessage>}
            <FormField label={t('account.name')} required error={errors.name === undefined ? refusals['name'] : t(USER_NAME_PROBLEM_KEYS[errors.name])}>
                {(control) => (
                    <Input {...control} autoComplete="username" maxLength={USER_NAME_MAX_LENGTH} value={values.name} onChange={change('name')} disabled={busy}/>
                )}
            </FormField>
            <FormField label={t('account.email')} required error={errors.email === undefined ? refusals['email'] : t(EMAIL_PROBLEM_KEYS[errors.email])}>
                {(control) => (
                    <Input {...control} type="email" autoComplete="email" maxLength={EMAIL_MAX_LENGTH} value={values.email} onChange={change('email')} disabled={busy}/>
                )}
            </FormField>
            <FormField label={t('account.password')} required error={errors.password === undefined ? undefined : t(PASSWORD_PROBLEM_KEYS[errors.password])}>
                {(control) => (
                    <Input {...control} type="password" autoComplete="new-password" value={values.password} onChange={change('password')} disabled={busy}/>
                )}
            </FormField>
            <FormField label={t('account.repeatPassword')} required error={errors.repeat === undefined ? undefined : t('validation.passwordsDiffer')}>
                {(control) => (
                    <Input {...control} type="password" autoComplete="new-password" value={values.repeat} onChange={change('repeat')} disabled={busy}/>
                )}
            </FormField>
            <FormActions>
                <Button type="submit" loading={busy}>{t('account.submitRegistration')}</Button>
            </FormActions>
        </Form>
    );
}
