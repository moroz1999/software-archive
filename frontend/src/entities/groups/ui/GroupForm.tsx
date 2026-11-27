import type {ReactNode} from 'react';
import {groupMemberRoleValues} from '@/shared/api';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {Form, FormActions, FormField, FormMessage, FormSection} from '@/shared/ui/form';
import {Input} from '@/shared/ui/input';
import {MemberRoleEditor} from '@/shared/ui/member-role-editor';
import {Textarea} from '@/shared/ui/textarea';
import {NAME_PROBLEM_KEYS} from '../config/messages';
import {fetchGroupPeople} from '../api/group-api';
import {useGroupForm} from '../model/group-form-context';
import {GroupTypesField} from './GroupTypesField';

export interface GroupFormProps {
    readonly submitLabel: string;
    /** The editor of the group's labels, wired to the form through useGroupForm. */
    readonly labels?: ReactNode;
    /** The editor of the group's locations, wired to the form through useGroupForm. */
    readonly locations?: ReactNode;
    /** More buttons before the submit button, such as Cancel. */
    /** Where imports took it from, shown and not edited. */
    readonly sources?: ReactNode;
    readonly actions?: ReactNode;
}

/** The fields of a group inside a GroupFormProvider, which holds their values and checks them. */
export function GroupForm({submitLabel, labels, locations, sources, actions}: GroupFormProps) {
    const {t} = useTranslation();
    const {values, errors, refusals, change, changeMembers, submit, busy, error} = useGroupForm();

    return (
        <Form onSubmit={submit}>
            {error !== undefined && <FormMessage>{error}</FormMessage>}
            <FormSection title={t('formSections.main')}>
                <FormField label={t('group.name')} required error={errors.name === undefined ? refusals.name : t(NAME_PROBLEM_KEYS[errors.name])}>
                    {(control) => <Input {...control} value={values.name} onChange={change('name')} disabled={busy}/>}
                </FormField>
                <GroupTypesField/>
                <FormField label={t('group.notes')} error={refusals.notes}>
                    {(control) => <Textarea {...control} value={values.notes} onChange={change('notes')} disabled={busy}/>}
                </FormField>
            </FormSection>
            {labels !== undefined && <FormSection title={t('formSections.names')}>{labels}</FormSection>}
            {locations !== undefined && <FormSection title={t('formSections.geography')}>{locations}</FormSection>}
            <FormSection title={t('formSections.members')}>
                <MemberRoleEditor
                    label={t('group.members')}
                    value={values.members ?? []}
                    onChange={changeMembers}
                    search={fetchGroupPeople}
                    roles={groupMemberRoleValues.map((code) => ({code, title: t(`roles.group.${code}`)}))}
                    disabled={busy}
                    error={refusals.members}
                />
            </FormSection>
            {sources !== undefined && <FormSection title={t('formSections.sources')} columns={1}>{sources}</FormSection>}
            <FormActions>
                {actions}
                <Button type="submit" loading={busy}>{submitLabel}</Button>
            </FormActions>
        </Form>
    );
}
