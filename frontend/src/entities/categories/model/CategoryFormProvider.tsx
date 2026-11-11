import {type ReactNode, useState} from 'react';
import {type BusinessError, type BusinessMessages, splitRefusal, unedited} from '@/shared/api';
import type {CategoryId} from './category';
import {type CategoryDraft, type CategoryFormErrors, type CategoryFormValues, toCategoryFormValues, validateCategoryForm} from './category-form';
import {CategoryFormContext, type CategoryFormState} from './category-form-context';

export interface CategoryFormProviderProps {
    /** Called only with values that pass the format rules. */
    readonly onSubmit: (draft: CategoryDraft) => void;
    readonly initial?: CategoryFormValues;
    /** The category being edited, none for a new one. */
    readonly editing?: CategoryId;
    readonly busy?: boolean;
    /** Why the last submit was refused (docs/error-handling.md). */
    readonly error?: BusinessError | undefined;
    /** The slice's texts for the codes of its business errors. */
    readonly messages?: BusinessMessages;
    readonly children: ReactNode;
}

/** The request field each form field is sent as: a refusal names it so. */
const REQUEST_FIELDS: Readonly<Record<keyof CategoryFormValues, string>> = {title: 'title', parent: 'parentId'};

/** The values of a category form being filled in, their format rules and why the last submit was refused. */
export function CategoryFormProvider({onSubmit, initial, editing, busy = false, error, messages = {}, children}: CategoryFormProviderProps) {
    const [values, setValues] = useState<CategoryFormValues>(() => initial ?? toCategoryFormValues());
    const [errors, setErrors] = useState<CategoryFormErrors>({});
    // Fields edited since the refusal arrived: their refusal no longer applies. A new refusal starts afresh.
    const [edited, setEdited] = useState<{readonly error: BusinessError | undefined; readonly fields: ReadonlySet<string>}>({error, fields: new Set()});
    if (edited.error !== error) {
        setEdited({error, fields: new Set()});
    }

    const refusal = splitRefusal(error, messages, Object.values(REQUEST_FIELDS));
    const fieldRefusals = unedited(refusal.fields, edited.fields);
    const titleRefusal = fieldRefusals[REQUEST_FIELDS.title];
    const parentRefusal = fieldRefusals[REQUEST_FIELDS.parent];

    const state: CategoryFormState = {
        values,
        errors,
        refusals: {
            ...(titleRefusal === undefined ? {} : {title: titleRefusal}),
            ...(parentRefusal === undefined ? {} : {parent: parentRefusal}),
        },
        change: (field) => (value) => {
            setValues((current) => ({...current, [field]: value}));
            if (field === 'title') {
                setErrors({});
            }
            const requestField = REQUEST_FIELDS[field];
            setEdited((current) => (current.fields.has(requestField) ? current : {...current, fields: new Set([...current.fields, requestField])}));
        },
        editing,
        submit: () => {
            const result = validateCategoryForm(values);
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

    return <CategoryFormContext value={state}>{children}</CategoryFormContext>;
}
