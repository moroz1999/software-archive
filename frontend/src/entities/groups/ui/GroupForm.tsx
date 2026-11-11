import type {ReactNode, SyntheticEvent} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {Field} from '@/shared/ui/field';
import {Inline} from '@/shared/ui/inline';
import {Input} from '@/shared/ui/input';
import {Stack} from '@/shared/ui/stack';
import {Textarea} from '@/shared/ui/textarea';
import {Text} from '@/shared/ui/typography';
import {NAME_PROBLEM_KEYS} from '../config/messages';
import {useGroupForm} from '../model/group-form-context';
import {GroupTypesField} from './GroupTypesField';
import styles from './GroupForm.module.css';

export interface GroupFormProps {
    readonly submitLabel: string;
    /** The editor of the group's labels, wired to the form through useGroupForm. */
    readonly labels?: ReactNode;
    /** The editor of the group's locations, wired to the form through useGroupForm. */
    readonly locations?: ReactNode;
    /** More buttons before the submit button, such as Cancel. */
    readonly actions?: ReactNode;
}

/** The fields of a group inside a GroupFormProvider, which holds their values and checks them. */
export function GroupForm({submitLabel, labels, locations, actions}: GroupFormProps) {
    const {t} = useTranslation();
    const {values, errors, refusals, change, submit, busy, error} = useGroupForm();

    function onSubmit(event: SyntheticEvent) {
        event.preventDefault();
        submit();
    }

    return (
        <form noValidate onSubmit={onSubmit} className={styles.form}>
            <Stack gap="lg">
                {error !== undefined && <Text role="alert" tone="danger">{error}</Text>}
                <Field label={t('group.name')} error={errors.name === undefined ? refusals.name : t(NAME_PROBLEM_KEYS[errors.name])}>
                    {(control) => <Input {...control} value={values.name} onChange={change('name')} disabled={busy}/>}
                </Field>
                <GroupTypesField/>
                {labels}
                {locations}
                <Field label={t('group.notes')} error={refusals.notes}>
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
