import {type ReactNode, useState} from 'react';
import {type BusinessError, type BusinessMessages, splitRefusal, unedited} from '@/shared/api';
import {type GroupDraft, type GroupFormErrors, type GroupFormValues, toGroupFormValues, validateGroupForm} from './group-form';
import {GroupFormContext, type GroupFormRefusals, type GroupFormState} from './group-form-context';

export interface GroupFormProviderProps {
    /** Called only with values that pass the format rules. */
    readonly onSubmit: (draft: GroupDraft) => void;
    readonly initial?: GroupFormValues;
    readonly busy?: boolean;
    /** Why the last submit was refused (docs/error-handling.md). */
    readonly error?: BusinessError | undefined;
    /** The slice's texts for the codes of its business errors. */
    readonly messages?: BusinessMessages;
    readonly children: ReactNode;
}

const FIELDS = ['name', 'notes', 'types', 'labels', 'locations'] as const satisfies readonly (keyof GroupFormValues)[];

function withoutErrorOf(errors: GroupFormErrors, field: keyof GroupFormValues): GroupFormErrors {
    return Object.fromEntries(Object.entries(errors).filter(([key]) => key !== field));
}

/** The values of a group form being filled in, their format rules and why the last submit was refused. */
export function GroupFormProvider({onSubmit, initial, busy = false, error, messages = {}, children}: GroupFormProviderProps) {
    const [values, setValues] = useState<GroupFormValues>(() => initial ?? toGroupFormValues());
    const [errors, setErrors] = useState<GroupFormErrors>({});
    // Fields edited since the refusal arrived: their refusal no longer applies. A new refusal starts afresh.
    const [edited, setEdited] = useState<{readonly error: BusinessError | undefined; readonly fields: ReadonlySet<string>}>({error, fields: new Set()});
    if (edited.error !== error) {
        setEdited({error, fields: new Set()});
    }

    const refusal = splitRefusal(error, messages, FIELDS);
    const refusals: GroupFormRefusals = unedited(refusal.fields, edited.fields);

    function edit(field: keyof GroupFormValues, values: Partial<GroupFormValues>) {
        setValues((current) => ({...current, ...values}));
        setErrors((current) => withoutErrorOf(current, field));
        setEdited((current) => (current.fields.has(field) ? current : {...current, fields: new Set([...current.fields, field])}));
    }

    const state: GroupFormState = {
        values,
        errors,
        refusals,
        change: (field) => (value) => {
            edit(field, {[field]: value});
        },
        changeTypes: (types) => {
            edit('types', {types});
        },
        changeLabels: (labels) => {
            edit('labels', {labels});
        },
        changeLocations: (locations) => {
            edit('locations', {locations});
        },
        submit: () => {
            const result = validateGroupForm(values);
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

    return <GroupFormContext value={state}>{children}</GroupFormContext>;
}
