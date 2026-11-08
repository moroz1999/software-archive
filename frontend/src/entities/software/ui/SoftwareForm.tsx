import {type ReactNode, type SyntheticEvent, useState} from 'react';
import {Button, Field, Inline, Input, Stack, Text, Textarea} from '../../../shared/ui';
import {RELEASE_DATE_INVALID_MESSAGE, TITLE_PROBLEM_MESSAGES} from '../config/messages';
import {type SoftwareDraft, type SoftwareFormErrors, type SoftwareFormValues, toSoftwareFormValues, validateSoftwareForm} from '../model/software-form';

export interface SoftwareFormProps {
    readonly submitLabel: string;
    /** Called only with values that pass the format rules. */
    readonly onSubmit: (draft: SoftwareDraft) => void;
    readonly initial?: SoftwareFormValues;
    /** Saving runs: the form is locked. */
    readonly busy?: boolean;
    /** A business error of the whole form, shown at its top. */
    readonly error?: string | undefined;
    /** More buttons next to the submit button. */
    readonly actions?: ReactNode;
}

/** The fields of software, checked before they are submitted. */
export function SoftwareForm({submitLabel, onSubmit, initial, busy = false, error, actions}: SoftwareFormProps) {
    const [values, setValues] = useState<SoftwareFormValues>(() => initial ?? toSoftwareFormValues());
    const [errors, setErrors] = useState<SoftwareFormErrors>({});

    function change(field: keyof SoftwareFormValues) {
        return (value: string) => {
            setValues((current) => ({...current, [field]: value}));
            setErrors((current) => (field in current ? {...current, [field]: undefined} : current));
        };
    }

    function submit(event: SyntheticEvent) {
        event.preventDefault();
        const result = validateSoftwareForm(values);
        if (result.ok) {
            setErrors({});
            onSubmit(result.draft);
        } else {
            setErrors(result.errors);
        }
    }

    return (
        <form noValidate onSubmit={submit}>
            <Stack gap="lg">
                {error !== undefined && <Text role="alert" tone="danger">{error}</Text>}
                <Field label="Title" error={errors.title === undefined ? undefined : TITLE_PROBLEM_MESSAGES[errors.title]}>
                    {(control) => <Input {...control} value={values.title} onChange={change('title')} disabled={busy}/>}
                </Field>
                <Field label="Description">
                    {(control) => <Textarea {...control} value={values.description} onChange={change('description')} disabled={busy}/>}
                </Field>
                <Field label="Release date" error={errors.releaseDate === undefined ? undefined : RELEASE_DATE_INVALID_MESSAGE}>
                    {(control) => <Input {...control} type="date" value={values.releaseDate} onChange={change('releaseDate')} disabled={busy}/>}
                </Field>
                <Inline gap="sm" justify="between" wrap>
                    <Button type="submit" loading={busy}>{submitLabel}</Button>
                    {actions}
                </Inline>
            </Stack>
        </form>
    );
}
