import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {Form, FormActions, FormField, FormMessage, FormSection} from '@/shared/ui/form';
import {Input} from '@/shared/ui/input';
import {Textarea} from '@/shared/ui/textarea';
import {REAL_NAME_PROBLEM_KEYS} from '../config/messages';
import {useAuthorForm} from '../model/author-form-context';

export interface AuthorFormProps {
    readonly submitLabel: string;
    /** The editor of the author's labels, wired to the form through useAuthorForm. */
    readonly labels?: ReactNode;
    /** The editor of the author's locations, wired to the form through useAuthorForm. */
    readonly locations?: ReactNode;
    /** More buttons before the submit button, such as Cancel. */
    readonly actions?: ReactNode;
}

/** The fields of an author inside an AuthorFormProvider, which holds their values and checks them. */
export function AuthorForm({submitLabel, labels, locations, actions}: AuthorFormProps) {
    const {t} = useTranslation();
    const {values, errors, refusals, change, submit, busy, error} = useAuthorForm();

    return (
        <Form onSubmit={submit}>
            {error !== undefined && <FormMessage>{error}</FormMessage>}
            <FormSection title={t('formSections.main')}>
                <FormField label={t('author.realName')} error={errors.realName === undefined ? refusals.realName : t(REAL_NAME_PROBLEM_KEYS[errors.realName])}>
                    {(control) => <Input {...control} value={values.realName} onChange={change('realName')} disabled={busy}/>}
                </FormField>
                <FormField label={t('author.notes')} error={refusals.notes}>
                    {(control) => <Textarea {...control} value={values.notes} onChange={change('notes')} disabled={busy}/>}
                </FormField>
            </FormSection>
            {labels !== undefined && <FormSection title={t('formSections.names')}>{labels}</FormSection>}
            {locations !== undefined && <FormSection title={t('formSections.geography')}>{locations}</FormSection>}
            <FormActions>
                {actions}
                <Button type="submit" loading={busy}>{submitLabel}</Button>
            </FormActions>
        </Form>
    );
}
