import {type ReactNode, useState} from 'react';
import {type BusinessError, type BusinessMessages, splitRefusal, unedited} from '@/shared/api';
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

const FIELDS = ['title', 'description', 'releaseDate', 'platform', 'hardware', 'categories'] as const satisfies readonly (keyof SoftwareFormValues)[];

/** The values of a software form being filled in, their format rules and why the last submit was refused. */
export function SoftwareFormProvider({onSubmit, initial, platforms = [], busy = false, error, messages = {}, children}: SoftwareFormProviderProps) {
    const [values, setValues] = useState<SoftwareFormValues>(() => initial ?? toSoftwareFormValues());
    const [errors, setErrors] = useState<SoftwareFormErrors>({});
    // Fields edited since the refusal arrived: their refusal no longer applies. A new refusal starts afresh.
    const [edited, setEdited] = useState<{readonly error: BusinessError | undefined; readonly fields: ReadonlySet<string>}>({error, fields: new Set()});
    if (edited.error !== error) {
        setEdited({error, fields: new Set()});
    }

    const refusal = splitRefusal(error, messages, FIELDS);
    const refusals: SoftwareFormRefusals = unedited(refusal.fields, edited.fields);

    function markEdited(field: keyof SoftwareFormValues) {
        setErrors((current) => (field in current ? {...current, [field]: undefined} : current));
        setEdited((current) => (current.fields.has(field) ? current : {...current, fields: new Set([...current.fields, field])}));
    }

    const state: SoftwareFormState = {
        values,
        errors,
        refusals,
        change: (field) => (value) => {
            setValues((current) => ({...current, [field]: value}));
            markEdited(field);
        },
        changeHardware: (codes) => {
            setValues((current) => ({...current, hardware: codes}));
            markEdited('hardware');
        },
        changeCategories: (ids) => {
            setValues((current) => ({...current, categories: ids}));
            markEdited('categories');
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
