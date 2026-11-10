import type {ReactNode, SyntheticEvent} from 'react';
import {Button} from '@/shared/ui/button';
import {Field} from '@/shared/ui/field';
import {Inline} from '@/shared/ui/inline';
import {Input} from '@/shared/ui/input';
import {Select} from '@/shared/ui/select';
import {Stack} from '@/shared/ui/stack';
import {Textarea} from '@/shared/ui/textarea';
import {Text} from '@/shared/ui/typography';
import {
    RELEASE_DATE_INVALID_MESSAGE,
    RELEASE_TYPE_REQUIRED_MESSAGE,
    RELEASE_VERSION_TOO_LONG_MESSAGE,
    TITLE_PROBLEM_MESSAGES,
} from '../config/messages';
import {releaseTypeText} from '../lib/release-type';
import {RELEASE_TYPES} from '../model/release';
import {useReleaseForm} from '../model/release-form-context';
import {HardwarePicker} from './HardwarePicker';

export interface ReleaseFormProps {
    readonly submitLabel: string;
    /** More buttons next to the submit button. */
    readonly actions?: ReactNode;
}

const TYPE_OPTIONS = RELEASE_TYPES.map((type) => ({value: type, label: releaseTypeText(type)}));

/** The fields of a release inside a ReleaseFormProvider, its hardware chosen from the platform of its software. */
export function ReleaseForm({submitLabel, actions}: ReleaseFormProps) {
    const {values, errors, refusals, change, submit, busy, error} = useReleaseForm();

    function onSubmit(event: SyntheticEvent) {
        event.preventDefault();
        submit();
    }

    return (
        <form noValidate onSubmit={onSubmit}>
            <Stack gap="lg">
                {error !== undefined && <Text role="alert" tone="danger">{error}</Text>}
                <Field label="Title" error={errors.title === undefined ? refusals.title : TITLE_PROBLEM_MESSAGES[errors.title]}>
                    {(control) => <Input {...control} value={values.title} onChange={change('title')} disabled={busy}/>}
                </Field>
                <Field label="Version" error={errors.version === undefined ? refusals.version : RELEASE_VERSION_TOO_LONG_MESSAGE}>
                    {(control) => <Input {...control} value={values.version} onChange={change('version')} disabled={busy}/>}
                </Field>
                <Field label="Type" error={errors.type === undefined ? refusals.type : RELEASE_TYPE_REQUIRED_MESSAGE}>
                    {(control) => (
                        <Select {...control} value={values.type} onChange={change('type')} options={TYPE_OPTIONS} placeholder="Choose a type" disabled={busy}/>
                    )}
                </Field>
                <Field label="Release date" error={errors.releaseDate === undefined ? refusals.releaseDate : RELEASE_DATE_INVALID_MESSAGE}>
                    {(control) => <Input {...control} type="date" value={values.releaseDate} onChange={change('releaseDate')} disabled={busy}/>}
                </Field>
                <Field label="Description" error={refusals.description}>
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
    const {values, refusals, changeHardware, hardware, busy} = useReleaseForm();
    return (
        <HardwarePicker
            hardware={hardware}
            value={values.hardware}
            onChange={changeHardware}
            disabled={busy}
            hint="A group checked here replaces that group of the software."
            error={refusals.hardware}
        />
    );
}
