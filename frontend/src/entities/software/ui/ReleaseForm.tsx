import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {Form, FormActions, FormField, FormFieldset, FormMessage, FormRow, FormSection} from '@/shared/ui/form';
import {Inline} from '@/shared/ui/inline';
import {Input} from '@/shared/ui/input';
import {Select} from '@/shared/ui/select';
import {Textarea} from '@/shared/ui/textarea';
import {FileUpload} from '@/shared/ui/file-upload';
import {Text} from '@/shared/ui/typography';
import {
    RELEASE_DATE_HINT_KEY,
    RELEASE_DATE_INVALID_KEY,
    RELEASE_TYPE_REQUIRED_KEY,
    RELEASE_VERSION_TOO_LONG_KEY,
    TITLE_PROBLEM_KEYS,
} from '../config/messages';
import {RELEASE_TYPES} from '../model/release';
import {useReleaseForm} from '../model/release-form-context';
import {fileSizeText} from '../model/release-archive';
import {HardwarePicker} from './HardwarePicker';

export interface ReleaseFormProps {
    readonly submitLabel: string;
    /** More buttons before the submit button, such as Cancel. */
    readonly actions?: ReactNode;
}

/** The fields of a release inside a ReleaseFormProvider, its hardware chosen from the platform of its software. */
export function ReleaseForm({submitLabel, actions}: ReleaseFormProps) {
    const {t} = useTranslation();
    const {values, errors, refusals, change, submit, busy, error} = useReleaseForm();
    const typeOptions = RELEASE_TYPES.map((type) => ({value: type, label: t(`release.types.${type}`)}));

    return (
        <Form onSubmit={submit} columns={2}>
            {error !== undefined && <FormMessage>{error}</FormMessage>}
            <FormSection title={t('formSections.main')}>
                <FormField label={t('fields.title')} fullWidth error={errors.title === undefined ? refusals.title : t(TITLE_PROBLEM_KEYS[errors.title])}>
                    {(control) => <Input {...control} value={values.title} onChange={change('title')} disabled={busy}/>}
                </FormField>
                <FormRow columns={3}>
                    <FormField label={t('fields.version')} error={errors.version === undefined ? refusals.version : t(RELEASE_VERSION_TOO_LONG_KEY)}>
                        {(control) => <Input {...control} value={values.version} onChange={change('version')} disabled={busy}/>}
                    </FormField>
                    <FormField label={t('fields.type')} required error={errors.type === undefined ? refusals.type : t(RELEASE_TYPE_REQUIRED_KEY)}>
                        {(control) => (
                            <Select {...control} value={values.type} onChange={change('type')} options={typeOptions} placeholder={t('fields.chooseType')} disabled={busy}/>
                        )}
                    </FormField>
                    <FormField
                        label={t('fields.releaseDate')}
                        hint={t(RELEASE_DATE_HINT_KEY)}
                        error={errors.releaseDate === undefined ? refusals.releaseDate : t(RELEASE_DATE_INVALID_KEY)}
                    >
                        {(control) => <Input {...control} value={values.releaseDate} onChange={change('releaseDate')} disabled={busy}/>}
                    </FormField>
                </FormRow>
                <FormField label={t('fields.description')} fullWidth error={refusals.description}>
                    {(control) => <Textarea {...control} value={values.description} onChange={change('description')} disabled={busy}/>}
                </FormField>
            </FormSection>
            <FormSection title={t('formSections.hardwareAndArchive')} columns={1}>
                <ReleaseHardware/>
                <ReleaseArchiveField/>
            </FormSection>
            <FormActions>
                {actions}
                <Button type="submit" loading={busy}>{submitLabel}</Button>
            </FormActions>
        </Form>
    );
}

function ReleaseArchiveField() {
    const {t} = useTranslation();
    const {archive, changeArchive, removeArchive, errors, refusals, busy} = useReleaseForm();
    const error = errors.archive === 'too-large' ? t('release.archiveErrors.size') : refusals.archive;
    return (
        <FormFieldset legend={t('release.archive')} error={error}>
            {archive === null ? null : (
                <Inline gap="sm" align="center" wrap>
                    <Text>{archive.fileName}</Text>
                    <Text variant="bodySm" tone="muted">{fileSizeText(archive.size)}</Text>
                </Inline>
            )}
            <Inline gap="sm" align="center" wrap>
                <FileUpload
                    label={t(archive === null ? 'release.addArchive' : 'release.replaceArchive')}
                    disabled={busy}
                    onFiles={(files) => {
                        const file = files[0];
                        if (file !== undefined) {
                            changeArchive(file);
                        }
                    }}
                />
                {archive !== null && <Button color="danger" disabled={busy} onClick={removeArchive}>{t('release.removeArchive')}</Button>}
            </Inline>
            {error === undefined && <Text as="span" variant="caption" tone="muted">{t('release.archiveHint')}</Text>}
        </FormFieldset>
    );
}

/** The hardware of the platform; a group chosen here replaces that group of the software (docs/domain/hardware.md). */
function ReleaseHardware() {
    const {t} = useTranslation();
    const {values, refusals, changeHardware, hardware, busy} = useReleaseForm();
    return (
        <HardwarePicker
            hardware={hardware}
            value={values.hardware}
            onChange={changeHardware}
            disabled={busy}
            hint={t('release.hardwareHint')}
            error={refusals.hardware}
        />
    );
}
