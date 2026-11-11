import {createRequiredContext} from '@/shared/lib/context';
import type {CategoryId} from './category';
import type {CategoryFormErrors, CategoryFormValues} from './category-form';

/** The texts of a refusal under the fields it names. */
export interface CategoryFormRefusals {
    readonly title?: string;
    readonly parent?: string;
}

export interface CategoryFormState {
    readonly values: CategoryFormValues;
    /** Format errors found on the last submit; a field's error clears once the field is edited. */
    readonly errors: CategoryFormErrors;
    /** Why the last submit was refused, under the fields the refusal names; a field's text clears once it is edited. */
    readonly refusals: CategoryFormRefusals;
    readonly change: (field: keyof CategoryFormValues) => (value: string) => void;
    /** The category being edited: it and its subcategories cannot be its parent. */
    readonly editing: CategoryId | undefined;
    /** Checks the format rules and hands the draft on only when they pass. */
    readonly submit: () => void;
    /** Saving runs: the form is locked. */
    readonly busy: boolean;
    /** Why the last submit was refused, for what no field takes: shown at the top of the form. */
    readonly error: string | undefined;
}

/** The form being filled in, for the fields inside a CategoryFormProvider. */
export const [CategoryFormContext, useCategoryForm] = createRequiredContext<CategoryFormState>('CategoryForm');
