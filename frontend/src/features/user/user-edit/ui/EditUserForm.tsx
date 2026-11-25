import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {EMAIL_MAX_LENGTH, EMAIL_PROBLEM_KEYS, type GrantedGroup, USER_NAME_MAX_LENGTH, USER_NAME_PROBLEM_KEYS} from '@/entities/users';
import {Button} from '@/shared/ui/button';
import {Checkbox} from '@/shared/ui/checkbox';
import {Form, FormActions, FormField, FormMessage, FormSection} from '@/shared/ui/form';
import {Input} from '@/shared/ui/input';
import {Text} from '@/shared/ui/typography';
import {useEditUser} from '../model/edit-user-context';
import {GRANTED_GROUPS} from '../model/user-form';

const GROUP_KEYS: Readonly<Record<GrantedGroup, 'users.groups.admin' | 'users.groups.moderator' | 'users.groups.ai-assistant'>> = {
    admin: 'users.groups.admin',
    moderator: 'users.groups.moderator',
    'ai-assistant': 'users.groups.ai-assistant',
};

export interface EditUserFormProps {
    /** More buttons before the submit button, such as Cancel. */
    readonly actions?: ReactNode;
}

/** The account inside an EditUserProvider: its name and email, and the groups of a confirmed user. */
export function EditUserForm({actions}: EditUserFormProps) {
    const {t} = useTranslation();
    const {user, values, errors, refusals, change, toggleGroup, submit, busy, error} = useEditUser();

    return (
        <Form onSubmit={submit}>
            {error !== undefined && <FormMessage>{error}</FormMessage>}
            <FormSection title={t('formSections.account')} columns={1}>
                <FormField label={t('account.name')} required error={errors.name === undefined ? refusals['name'] : t(USER_NAME_PROBLEM_KEYS[errors.name])}>
                    {(control) => (
                        <Input {...control} autoComplete="off" maxLength={USER_NAME_MAX_LENGTH} value={values.name} onChange={change('name')} disabled={busy}/>
                    )}
                </FormField>
                <FormField label={t('account.email')} required error={errors.email === undefined ? refusals['email'] : t(EMAIL_PROBLEM_KEYS[errors.email])}>
                    {(control) => (
                        <Input {...control} type="email" autoComplete="off" maxLength={EMAIL_MAX_LENGTH} value={values.email} onChange={change('email')} disabled={busy}/>
                    )}
                </FormField>
            </FormSection>
            <FormSection title={t('formSections.groups')} columns={1}>
                {user.confirmed
                    ? GRANTED_GROUPS.map((group) => (
                        <Checkbox
                            key={group}
                            label={t(GROUP_KEYS[group])}
                            checked={values.groups.has(group)}
                            disabled={busy}
                            onChange={(on) => {
                                toggleGroup(group, on);
                            }}
                        />
                    ))
                    : null}
                <Text variant="caption" tone="muted">{t(user.confirmed ? 'users.memberNote' : 'users.unconfirmedNote')}</Text>
            </FormSection>
            <FormActions>
                {actions}
                <Button type="submit" loading={busy}>{t('actions.save')}</Button>
            </FormActions>
        </Form>
    );
}
