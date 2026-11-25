import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {fetchLabelOwners, LABEL_NAME_PROBLEM_KEYS, type LabelDetails} from '@/entities/labels';
import {COUNTRY_REQUIRED_KEY, LocationsEditor} from '@/entities/locations';
import {TITLE_MAX_LENGTH} from '@/shared/lib/form-rules';
import {Button} from '@/shared/ui/button';
import {Checkbox} from '@/shared/ui/checkbox';
import {Form, FormActions, FormField, FormFieldset, FormMessage, FormSection} from '@/shared/ui/form';
import {Inline} from '@/shared/ui/inline';
import {Input} from '@/shared/ui/input';
import {MultiEntityAutocomplete} from '@/shared/ui/multi-entity-autocomplete';
import {OriginList} from '@/shared/ui/origin-list';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {useEditLabel} from '../model/edit-label-context';

export interface EditLabelFormProps {
    /** More buttons before the submit button, such as Cancel. */
    readonly actions?: ReactNode;
}

/** The label inside an EditLabelProvider: its name and whose it is, its locations, and where imports took it from. */
export function EditLabelForm({actions}: EditLabelFormProps) {
    const {t} = useTranslation();
    const {label, values, errors, refusals, changeName, changeLocations, submit, busy, error} = useEditLabel();

    return (
        <Form onSubmit={submit}>
            {error !== undefined && <FormMessage>{error}</FormMessage>}
            <FormSection title={t('formSections.main')} columns={1}>
                <FormField label={t('label.nameField')} required error={errors.name === undefined ? undefined : t(LABEL_NAME_PROBLEM_KEYS[errors.name])}>
                    {(control) => (
                        <Input {...control} autoComplete="off" maxLength={TITLE_MAX_LENGTH} value={values.name} onChange={changeName} disabled={busy}/>
                    )}
                </FormField>
                <FormFieldset legend={t('label.owner')} error={refusals['owner']}>
                    {label.canChangeOwner ? <OwnerChoice/> : <OwnerShown label={label}/>}
                </FormFieldset>
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
            <FormSection title={t('formSections.sources')} columns={1}>
                <OriginList origins={label.origins}/>
            </FormSection>
            <FormActions>
                {actions}
                <Button type="submit" loading={busy}>{t('actions.save')}</Button>
            </FormActions>
        </Form>
    );
}

/** For administrators: the person or group the label belongs to, chosen by name, and for a person chosen anew whether it is their display name. */
function OwnerChoice() {
    const {t} = useTranslation();
    const {values, offersDisplay, changeOwner, changeDisplay, busy} = useEditLabel();
    return (
        <Stack gap="sm">
            <FormField label={t('fields.participant')}>
                {(control) => (
                    <MultiEntityAutocomplete
                        {...control}
                        value={values.owner === null ? [] : [values.owner]}
                        onChange={(chosen) => {
                            changeOwner(chosen.at(-1) ?? null);
                        }}
                        load={fetchLabelOwners}
                        queryKey="label-owner"
                        disabled={busy}
                    />
                )}
            </FormField>
            {offersDisplay && <Checkbox label={t('label.display')} checked={values.display} onChange={changeDisplay} disabled={busy}/>}
        </Stack>
    );
}

/** For anybody else: whose the label is, or that it is not known yet. */
function OwnerShown({label}: {readonly label: LabelDetails}) {
    const {t} = useTranslation();
    return label.owner === null
        ? <Text tone="muted">{t('label.ownerUnknown')}</Text>
        : (
            <Inline gap="sm" align="baseline" wrap>
                <Text>{label.owner.name}</Text>
                {label.owner.note !== null && <Text variant="bodySm" tone="muted">{label.owner.note}</Text>}
            </Inline>
        );
}
