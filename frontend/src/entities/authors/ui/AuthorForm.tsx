import type {ReactNode, SyntheticEvent} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {Field} from '@/shared/ui/field';
import {Inline} from '@/shared/ui/inline';
import {Input} from '@/shared/ui/input';
import {Stack} from '@/shared/ui/stack';
import {Textarea} from '@/shared/ui/textarea';
import {Text} from '@/shared/ui/typography';
import {REAL_NAME_PROBLEM_KEYS} from '../config/messages';
import {useAuthorForm} from '../model/author-form-context';
import styles from './AuthorForm.module.css';

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

    function onSubmit(event: SyntheticEvent) {
        event.preventDefault();
        submit();
    }

    return (
        <form noValidate onSubmit={onSubmit} className={styles.form}>
            <Stack gap="lg">
                {error !== undefined && <Text role="alert" tone="danger">{error}</Text>}
                <Field label={t('author.realName')} error={errors.realName === undefined ? refusals.realName : t(REAL_NAME_PROBLEM_KEYS[errors.realName])}>
                    {(control) => <Input {...control} value={values.realName} onChange={change('realName')} disabled={busy}/>}
                </Field>
                {labels}
                {locations}
                <Field label={t('author.notes')} error={refusals.notes}>
                    {(control) => <Textarea {...control} value={values.notes} onChange={change('notes')} disabled={busy}/>}
                </Field>
                <Inline gap="sm" justify="end" wrap>
                    {actions}
                    <Button type="submit" loading={busy}>{submitLabel}</Button>
                </Inline>
            </Stack>
        </form>
    );
}
