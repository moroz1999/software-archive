import type {ReactNode, SyntheticEvent} from 'react';
import {Button} from '@/shared/ui/button';
import {Field} from '@/shared/ui/field';
import {Inline} from '@/shared/ui/inline';
import {Input} from '@/shared/ui/input';
import {Stack} from '@/shared/ui/stack';
import {Textarea} from '@/shared/ui/textarea';
import {Text} from '@/shared/ui/typography';
import {RELEASE_DATE_INVALID_MESSAGE, TITLE_PROBLEM_MESSAGES} from '../config/messages';
import {useSoftwareForm} from '../model/software-form-context';

export interface SoftwareFormProps {
    readonly submitLabel: string;
    /** More buttons next to the submit button. */
    readonly actions?: ReactNode;
}

/** The fields of software inside a SoftwareFormProvider, which holds their values and checks them. */
export function SoftwareForm({submitLabel, actions}: SoftwareFormProps) {
    const {values, errors, refusals, change, submit, busy, error} = useSoftwareForm();

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
                <Field label="Description" error={refusals.description}>
                    {(control) => <Textarea {...control} value={values.description} onChange={change('description')} disabled={busy}/>}
                </Field>
                <Field label="Release date" error={errors.releaseDate === undefined ? refusals.releaseDate : RELEASE_DATE_INVALID_MESSAGE}>
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
