import {type ReactNode, useState} from 'react';
import {type BusinessError, type BusinessMessages, splitRefusal, unedited} from '@/shared/api';
import type {UiLanguage} from '@/shared/lib/i18n';
import {type CategoryDraft, type CategoryFormErrors, type CategoryFormValues, toCategoryFormValues, validateCategoryForm} from './category-form';
import {CategoryFormContext, type CategoryFormState} from './category-form-context';

export interface CategoryFormProviderProps {
    /** Called only with values that pass the format rules. */
    readonly onSubmit: (draft: CategoryDraft) => void;
    readonly initial?: CategoryFormValues;
    /** The category being edited, none for a new one. */
    readonly editing?: string;
    readonly busy?: boolean;
    /** Why the last submit was refused (docs/error-handling.md). */
    readonly error?: BusinessError | undefined;
    /** The slice's texts for the codes of its business errors. */
    readonly messages?: BusinessMessages;
    readonly children: ReactNode;
}

/** The request field each form field is sent as: a refusal names it so. */
const REQUEST_FIELDS: Readonly<Record<keyof CategoryFormValues, string>> = {titles: 'titles', parent: 'parentId'};

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
    const titlesRefusal = fieldRefusals[REQUEST_FIELDS.titles];
    const parentRefusal = fieldRefusals[REQUEST_FIELDS.parent];

    const markEdited = (field: keyof CategoryFormValues) => {
        const requestField = REQUEST_FIELDS[field];
        setEdited((current) => (current.fields.has(requestField) ? current : {...current, fields: new Set([...current.fields, requestField])}));
    };

    const state: CategoryFormState = {
        values,
        errors,
        refusals: {
            ...(titlesRefusal === undefined ? {} : {titles: titlesRefusal}),
            ...(parentRefusal === undefined ? {} : {parent: parentRefusal}),
        },
        changeTitle: (language: UiLanguage) => (value) => {
            setValues((current) => ({...current, titles: {...current.titles, [language]: value}}));
            setErrors({});
            markEdited('titles');
        },
        changeParent: (value) => {
            setValues((current) => ({...current, parent: value}));
            markEdited('parent');
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
