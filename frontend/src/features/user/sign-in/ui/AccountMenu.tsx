import {useCallback, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {useLocation} from 'react-router';
import {PASSWORD_PROBLEM_KEYS, REGISTER_PATH, useCurrentUser} from '@/entities/users';
import {Button} from '@/shared/ui/button';
import {Checkbox} from '@/shared/ui/checkbox';
import {Form, FormActions, FormField, FormMessage} from '@/shared/ui/form';
import {PersonIcon} from '@/shared/ui/icon';
import {IconPopover} from '@/shared/ui/icon-popover';
import {Inline} from '@/shared/ui/inline';
import {Input} from '@/shared/ui/input';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {useSignIn} from '../model/sign-in-context';
import {SignInProvider} from '../model/SignInProvider';

/**
 * The account button of the header and its window: the sign-in form for a visitor, the user's name and Sign out for a
 * signed-in user. The window closes once the visitor goes anywhere.
 */
export function AccountMenu() {
    const {t} = useTranslation();
    const location = useLocation();
    // Whether the window is open is UI state: it remembers where it was opened, and is closed anywhere else.
    const [openAt, setOpenAt] = useState<string | null>(null);
    if (openAt !== null && openAt !== location.key) {
        setOpenAt(null);
    }
    const onOpenChange = useCallback((open: boolean) => {
        setOpenAt(open ? location.key : null);
    }, [location.key]);

    return (
        <SignInProvider>
            <IconPopover label={t('account.menu')} icon={<PersonIcon/>} open={openAt !== null} onOpenChange={onOpenChange}>
                <Account/>
            </IconPopover>
        </SignInProvider>
    );
}

function Account() {
    const {t} = useTranslation();
    const {user} = useCurrentUser();
    const {signOut, signingOut} = useSignIn();
    if (user === null) {
        return <SignInForm/>;
    }
    return (
        <Stack gap="md">
            <Text variant="title">{user.name}</Text>
            <Inline justify="end">
                <Button color="secondary" onClick={signOut} loading={signingOut}>{t('account.signOut')}</Button>
            </Inline>
        </Stack>
    );
}

function SignInForm() {
    const {t} = useTranslation();
    const {values, errors, change, changeRemember, submit, busy, error, unconfirmed, resend, resending, resent} = useSignIn();

    return (
        <Form onSubmit={submit}>
            {error !== undefined && <FormMessage>{error}</FormMessage>}
            {unconfirmed && (resent
                ? <FormMessage variant="success">{t('account.resent')}</FormMessage>
                : <Button color="outlined" size="sm" onClick={resend} loading={resending}>{t('account.resend')}</Button>)}
            <FormField label={t('account.login')} required error={errors.login === undefined ? undefined : t('validation.loginRequired')}>
                {(control) => <Input {...control} autoComplete="username" value={values.login} onChange={change('login')} disabled={busy}/>}
            </FormField>
            <FormField label={t('account.password')} required error={errors.password === undefined ? undefined : t(PASSWORD_PROBLEM_KEYS[errors.password])}>
                {(control) => (
                    <Input {...control} type="password" autoComplete="current-password" value={values.password} onChange={change('password')} disabled={busy}/>
                )}
            </FormField>
            <Checkbox label={t('account.remember')} checked={values.remember} onChange={changeRemember} disabled={busy}/>
            <FormActions align="between">
                <Button href={REGISTER_PATH} color="transparent" size="sm">{t('account.register')}</Button>
                <Button type="submit" loading={busy}>{t('account.signIn')}</Button>
            </FormActions>
        </Form>
    );
}
