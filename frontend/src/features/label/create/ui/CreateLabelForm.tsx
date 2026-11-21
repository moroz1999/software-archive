import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {LABEL_NAME_PROBLEM_KEYS} from '@/entities/labels';
import {COUNTRY_REQUIRED_KEY, LocationsEditor} from '@/entities/locations';
import {TITLE_MAX_LENGTH} from '@/shared/lib/form-rules';
import {Button} from '@/shared/ui/button';
import {Form, FormActions, FormField, FormMessage, FormSection} from '@/shared/ui/form';
import {Input} from '@/shared/ui/input';
import {useCreateLabel} from '../model/create-label-context';

export interface CreateLabelFormProps {
    /** More buttons before the submit button, such as Cancel. */
    readonly actions?: ReactNode;
}

/** A new label of nobody inside a CreateLabelProvider: its name and its locations. */
export function CreateLabelForm({actions}: CreateLabelFormProps) {
    const {t} = useTranslation();
    const {values, errors, refusals, changeName, changeLocations, submit, busy, error} = useCreateLabel();

    return (
        <Form onSubmit={submit}>
            {error !== undefined && <FormMessage>{error}</FormMessage>}
            <FormSection title={t('formSections.main')} columns={1}>
                <FormField label={t('label.nameField')} required error={errors.name === undefined ? undefined : t(LABEL_NAME_PROBLEM_KEYS[errors.name])}>
                    {(control) => (
                        <Input {...control} autoComplete="off" maxLength={TITLE_MAX_LENGTH} value={values.name} onChange={changeName} disabled={busy}/>
                    )}
                </FormField>
            </FormSection>
            <FormSection title={t('formSections.geography')} columns={1}>
                <LocationsEditor
                    value={values.locations}
                    onChange={changeLocations}
                    rowErrors={errors.locations?.map((problem) => (problem === null ? undefined : t(COUNTRY_REQUIRED_KEY)))}
                    error={refusals['locations']}
                    disabled={busy}
                />
            </FormSection>
            <FormActions>
                {actions}
                <Button type="submit" loading={busy}>{t('actions.addLabel')}</Button>
            </FormActions>
        </Form>
    );
}
