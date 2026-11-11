import {type ReactNode, useState} from 'react';
import {type BusinessError, type BusinessMessages, splitRefusal, unedited} from '@/shared/api';
import {type AuthorDraft, type AuthorFormErrors, type AuthorFormValues, toAuthorFormValues, validateAuthorForm} from './author-form';
import {AuthorFormContext, type AuthorFormRefusals, type AuthorFormState} from './author-form-context';

export interface AuthorFormProviderProps {
    /** Called only with values that pass the format rules. */
    readonly onSubmit: (draft: AuthorDraft) => void;
    readonly initial?: AuthorFormValues;
    readonly busy?: boolean;
    /** Why the last submit was refused (docs/error-handling.md). */
    readonly error?: BusinessError | undefined;
    /** The slice's texts for the codes of its business errors. */
    readonly messages?: BusinessMessages;
    readonly children: ReactNode;
}

const FIELDS = ['realName', 'notes', 'labels', 'locations'] as const satisfies readonly (keyof AuthorFormValues)[];

/** The errors left once a field is edited: a real name and the labels both answer that an author needs a name. */
function withoutErrorOf(errors: AuthorFormErrors, field: keyof AuthorFormValues): AuthorFormErrors {
    const realName = field === 'realName' || (field === 'labels' && errors.realName === 'nameless') ? undefined : errors.realName;
    return {
        ...(realName === undefined ? {} : {realName}),
        ...(field === 'labels' || errors.labels === undefined ? {} : {labels: errors.labels}),
        ...(field === 'locations' || errors.locations === undefined ? {} : {locations: errors.locations}),
    };
}

/** The values of an author form being filled in, their format rules and why the last submit was refused. */
export function AuthorFormProvider({onSubmit, initial, busy = false, error, messages = {}, children}: AuthorFormProviderProps) {
    const [values, setValues] = useState<AuthorFormValues>(() => initial ?? toAuthorFormValues());
    const [errors, setErrors] = useState<AuthorFormErrors>({});
    // Fields edited since the refusal arrived: their refusal no longer applies. A new refusal starts afresh.
    const [edited, setEdited] = useState<{readonly error: BusinessError | undefined; readonly fields: ReadonlySet<string>}>({error, fields: new Set()});
    if (edited.error !== error) {
        setEdited({error, fields: new Set()});
    }

    const refusal = splitRefusal(error, messages, FIELDS);
    const refusals: AuthorFormRefusals = unedited(refusal.fields, edited.fields);

    function edit(field: keyof AuthorFormValues, values: Partial<AuthorFormValues>) {
        setValues((current) => ({...current, ...values}));
        setErrors((current) => withoutErrorOf(current, field));
        setEdited((current) => (current.fields.has(field) ? current : {...current, fields: new Set([...current.fields, field])}));
    }

    const state: AuthorFormState = {
        values,
        errors,
        refusals,
        change: (field) => (value) => {
            edit(field, {[field]: value});
        },
        changeLabels: (labels) => {
            edit('labels', {labels});
        },
        changeLocations: (locations) => {
            edit('locations', {locations});
        },
        submit: () => {
            const result = validateAuthorForm(values);
            if (result.ok) {
                setErrors({});
                onSubmit(result.draft);
            } else {
                setErrors(result.errors);
            }
        },
        busy,
        error: refusal.form,
    };

    return <AuthorFormContext value={state}>{children}</AuthorFormContext>;
}
