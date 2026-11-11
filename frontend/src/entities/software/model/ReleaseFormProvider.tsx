import {type ReactNode, useState} from 'react';
import {type BusinessError, type BusinessMessages, splitRefusal, unedited} from '@/shared/api';
import {type ReleaseDraft, type ReleaseFormErrors, type ReleaseFormValues, toReleaseFormValues, validateReleaseForm} from './release-form';
import {type ReleaseFormField, ReleaseFormContext, type ReleaseFormState} from './release-form-context';
import type {Hardware} from './software';

export interface ReleaseFormProviderProps {
    /** Called only with values that pass the format rules. */
    readonly onSubmit: (draft: ReleaseDraft) => void;
    readonly initial?: ReleaseFormValues;
    /** The hardware to choose from. */
    readonly hardware?: readonly Hardware[];
    readonly busy?: boolean;
    /** Why the last submit was refused (docs/error-handling.md). */
    readonly error?: BusinessError | undefined;
    /** The slice's texts for the codes of its business errors. */
    readonly messages?: BusinessMessages;
    readonly children: ReactNode;
}

const FIELDS = ['title', 'version', 'type', 'releaseDate', 'description', 'hardware'] as const satisfies readonly ReleaseFormField[];

/** The values of a release form being filled in, their format rules and why the last submit was refused. */
export function ReleaseFormProvider({onSubmit, initial, hardware = [], busy = false, error, messages = {}, children}: ReleaseFormProviderProps) {
    const [values, setValues] = useState<ReleaseFormValues>(() => initial ?? toReleaseFormValues());
    const [errors, setErrors] = useState<ReleaseFormErrors>({});
    // Fields edited since the refusal arrived: their refusal no longer applies. A new refusal starts afresh.
    const [edited, setEdited] = useState<{readonly error: BusinessError | undefined; readonly fields: ReadonlySet<string>}>({error, fields: new Set()});
    if (edited.error !== error) {
        setEdited({error, fields: new Set()});
    }

    function markEdited(field: ReleaseFormField) {
        setErrors((current) => (field in current ? {...current, [field]: undefined} : current));
        setEdited((current) => (current.fields.has(field) ? current : {...current, fields: new Set([...current.fields, field])}));
    }

    const refusal = splitRefusal(error, messages, FIELDS);

    const state: ReleaseFormState = {
        values,
        errors,
        refusals: unedited(refusal.fields, edited.fields),
        change: (field) => (value) => {
            setValues((current) => ({...current, [field]: value}));
            markEdited(field);
        },
        changeHardware: (codes) => {
            setValues((current) => ({...current, hardware: codes}));
            markEdited('hardware');
        },
        submit: () => {
            const result = validateReleaseForm(values);
            if (result.ok) {
                setErrors({});
                onSubmit(result.draft);
            } else {
                setErrors(result.errors);
            }
        },
        hardware,
        busy,
        error: refusal.form,
    };

    return <ReleaseFormContext value={state}>{children}</ReleaseFormContext>;
}
