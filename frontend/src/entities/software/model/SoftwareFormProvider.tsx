import {type ReactNode, useState} from 'react';
import {type BusinessError, businessErrorMessage, type BusinessMessages} from '@/shared/api';
import {type PlatformChoice, type SoftwareDraft, type SoftwareFormErrors, type SoftwareFormValues, toSoftwareFormValues, validateSoftwareForm} from './software-form';
import {type SoftwareFormRefusals, SoftwareFormContext, type SoftwareFormState} from './software-form-context';

export interface SoftwareFormProviderProps {
    /** Called only with values that pass the format rules. */
    readonly onSubmit: (draft: SoftwareDraft) => void;
    readonly initial?: SoftwareFormValues;
    /** The platforms to choose from. */
    readonly platforms?: readonly PlatformChoice[];
    readonly busy?: boolean;
    /** Why the last submit was refused (docs/error-handling.md). */
    readonly error?: BusinessError | undefined;
    /** The slice's texts for the codes of its business errors. */
    readonly messages?: BusinessMessages;
    readonly children: ReactNode;
}

const FIELDS: readonly string[] = ['title', 'description', 'releaseDate', 'platform'] satisfies (keyof SoftwareFormValues)[];

function isField(field: string): field is keyof SoftwareFormValues {
    return FIELDS.includes(field);
}

/** A refusal split into the texts under its fields and the text of the whole form, for what no field takes. */
function splitRefusal(error: BusinessError | undefined, messages: BusinessMessages): {fields: SoftwareFormRefusals; form: string | undefined} {
    if (error === undefined) {
        return {fields: {}, form: undefined};
    }
    const fields: Partial<Record<keyof SoftwareFormValues, string>> = {};
    let fieldless = error.fieldErrors.length === 0;
    for (const fieldError of error.fieldErrors) {
        if (isField(fieldError.field)) {
            fields[fieldError.field] = messages[fieldError.code] ?? businessErrorMessage(error, messages);
        } else {
            fieldless = true;
        }
    }
    return {fields, form: fieldless ? businessErrorMessage(error, messages) : undefined};
}

/** The values of a software form being filled in, their format rules and why the last submit was refused. */
export function SoftwareFormProvider({onSubmit, initial, platforms = [], busy = false, error, messages = {}, children}: SoftwareFormProviderProps) {
    const [values, setValues] = useState<SoftwareFormValues>(() => initial ?? toSoftwareFormValues());
    const [errors, setErrors] = useState<SoftwareFormErrors>({});
    // Fields edited since the refusal arrived: their refusal no longer applies. A new refusal starts afresh.
    const [edited, setEdited] = useState<{readonly error: BusinessError | undefined; readonly fields: ReadonlySet<string>}>({error, fields: new Set()});
    if (edited.error !== error) {
        setEdited({error, fields: new Set()});
    }

    const refusal = splitRefusal(error, messages);
    const refusals: SoftwareFormRefusals = Object.fromEntries(
        Object.entries(refusal.fields).filter(([field]) => !edited.fields.has(field)),
    );

    const state: SoftwareFormState = {
        values,
        errors,
        refusals,
        change: (field) => (value) => {
            setValues((current) => ({...current, [field]: value}));
            setErrors((current) => (field in current ? {...current, [field]: undefined} : current));
            setEdited((current) => (current.fields.has(field) ? current : {...current, fields: new Set([...current.fields, field])}));
        },
        submit: () => {
            const result = validateSoftwareForm(values);
            if (result.ok) {
                setErrors({});
                onSubmit(result.draft);
            } else {
                setErrors(result.errors);
            }
        },
        platforms,
        busy,
        error: refusal.form,
    };

    return <SoftwareFormContext value={state}>{children}</SoftwareFormContext>;
}
