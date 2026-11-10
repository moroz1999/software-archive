import type {ReactNode, SyntheticEvent} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {Field} from '@/shared/ui/field';
import {Inline} from '@/shared/ui/inline';
import {Input} from '@/shared/ui/input';
import {Select} from '@/shared/ui/select';
import {Stack} from '@/shared/ui/stack';
import {Textarea} from '@/shared/ui/textarea';
import {Text} from '@/shared/ui/typography';
import {
    RELEASE_DATE_INVALID_KEY,
    RELEASE_TYPE_REQUIRED_KEY,
    RELEASE_VERSION_TOO_LONG_KEY,
    TITLE_PROBLEM_KEYS,
} from '../config/messages';
import {RELEASE_TYPES} from '../model/release';
import {useReleaseForm} from '../model/release-form-context';
import {HardwarePicker} from './HardwarePicker';

export interface ReleaseFormProps {
    readonly submitLabel: string;
    /** More buttons next to the submit button. */
    readonly actions?: ReactNode;
}

/** The fields of a release inside a ReleaseFormProvider, its hardware chosen from the platform of its software. */
export function ReleaseForm({submitLabel, actions}: ReleaseFormProps) {
    const {t} = useTranslation();
    const {values, errors, refusals, change, submit, busy, error} = useReleaseForm();
    const typeOptions = RELEASE_TYPES.map((type) => ({value: type, label: t(`release.types.${type}`)}));

    function onSubmit(event: SyntheticEvent) {
        event.preventDefault();
        submit();
    }

    return (
        <form noValidate onSubmit={onSubmit}>
            <Stack gap="lg">
                {error !== undefined && <Text role="alert" tone="danger">{error}</Text>}
                <Field label={t('fields.title')} error={errors.title === undefined ? refusals.title : t(TITLE_PROBLEM_KEYS[errors.title])}>
                    {(control) => <Input {...control} value={values.title} onChange={change('title')} disabled={busy}/>}
                </Field>
                <Field label={t('fields.version')} error={errors.version === undefined ? refusals.version : t(RELEASE_VERSION_TOO_LONG_KEY)}>
                    {(control) => <Input {...control} value={values.version} onChange={change('version')} disabled={busy}/>}
                </Field>
                <Field label={t('fields.type')} error={errors.type === undefined ? refusals.type : t(RELEASE_TYPE_REQUIRED_KEY)}>
                    {(control) => (
                        <Select {...control} value={values.type} onChange={change('type')} options={typeOptions} placeholder={t('fields.chooseType')} disabled={busy}/>
                    )}
                </Field>
                <Field label={t('fields.releaseDate')} error={errors.releaseDate === undefined ? refusals.releaseDate : t(RELEASE_DATE_INVALID_KEY)}>
                    {(control) => <Input {...control} type="date" value={values.releaseDate} onChange={change('releaseDate')} disabled={busy}/>}
                </Field>
                <Field label={t('fields.description')} error={refusals.description}>
                    {(control) => <Textarea {...control} value={values.description} onChange={change('description')} disabled={busy}/>}
                </Field>
                <ReleaseHardware/>
                <Inline gap="sm" justify="between" wrap>
                    <Button type="submit" loading={busy}>{submitLabel}</Button>
                    {actions}
                </Inline>
            </Stack>
        </form>
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
