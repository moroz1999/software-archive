import {type TitleProblem, titleProblem} from '@/shared/lib/title';
import type {CategoryDetails} from './category';

/** What the category form holds while it is being filled: the id of the chosen parent, empty for the top. */
export interface CategoryFormValues {
    readonly title: string;
    readonly parent: string;
}

/** A category to be created or saved, as the form produced it. */
export interface CategoryDraft {
    readonly title: string;
    readonly parentId: string | null;
}

export interface CategoryFormErrors {
    readonly title?: TitleProblem;
}

export type CategoryFormResult =
    | {readonly ok: true; readonly draft: CategoryDraft}
    | {readonly ok: false; readonly errors: CategoryFormErrors};

/** Checks the format rules of the form: a category has a title; without a parent it goes to the top. */
export function validateCategoryForm(values: CategoryFormValues): CategoryFormResult {
    const title = titleProblem(values.title);
    if (title !== null) {
        return {ok: false, errors: {title}};
    }
    return {ok: true, draft: {title: values.title, parentId: values.parent === '' ? null : values.parent}};
}

/** The form values of an existing category, or of an empty form. */
export function toCategoryFormValues(category?: CategoryDetails): CategoryFormValues {
    return {title: category?.title ?? '', parent: category?.parentId ?? ''};
}
