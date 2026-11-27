import {perUiLanguage, SUPPORTED_UI_LANGUAGES, type UiLanguage} from '@/shared/lib/i18n';
import {type TitleProblem, titleProblem} from '@/shared/lib/form-rules';
import type {CategoryDetails, CategoryTitles} from './category';

/**
 * What the category form holds while it is being filled: a title per language, empty when not given, and the id of
 * the chosen parent, empty for the top.
 */
export interface CategoryFormValues {
    readonly titles: Readonly<Record<UiLanguage, string>>;
    readonly parent: string;
}

/** A category to be created or saved, as the form produced it: only the titles given. */
export interface CategoryDraft {
    readonly titles: CategoryTitles;
    readonly parentId: string | null;
}

export interface CategoryFormErrors {
    readonly titles?: Readonly<Partial<Record<UiLanguage, TitleProblem>>>;
}

export type CategoryFormResult =
    | {readonly ok: true; readonly draft: CategoryDraft}
    | {readonly ok: false; readonly errors: CategoryFormErrors};

/** The languages of the titles in the order the form asks for them: Russian, which every category has, first. */
export const CATEGORY_TITLE_LANGUAGES: readonly UiLanguage[] = ['ru', ...SUPPORTED_UI_LANGUAGES.filter((language) => language !== 'ru')];

/**
 * Checks the format rules of the form: a category has a Russian title, a title in another language only when it is
 * given; every title is within the length limit. Without a parent the category goes to the top.
 */
export function validateCategoryForm(values: CategoryFormValues): CategoryFormResult {
    const problems: Partial<Record<UiLanguage, TitleProblem>> = {};
    const given: Partial<Record<UiLanguage, string>> = {};
    for (const language of CATEGORY_TITLE_LANGUAGES) {
        const title = values.titles[language];
        if (language !== 'ru' && title.trim() === '') {
            continue;
        }
        const problem = titleProblem(title);
        if (problem === null) {
            given[language] = title;
        } else {
            problems[language] = problem;
        }
    }
    if (Object.keys(problems).length > 0 || given.ru === undefined) {
        return {ok: false, errors: {titles: problems}};
    }
    return {ok: true, draft: {titles: {...given, ru: given.ru}, parentId: values.parent === '' ? null : values.parent}};
}

/** The form values of an existing category, or of an empty form. */
export function toCategoryFormValues(category?: CategoryDetails): CategoryFormValues {
    const titles = perUiLanguage((language) => category?.titles[language] ?? '');
    return {titles, parent: category?.parentId ?? ''};
}
