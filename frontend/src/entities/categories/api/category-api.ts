import {api, apiError, type components} from '@/shared/api';
import {SUPPORTED_UI_LANGUAGES, type UiLanguage} from '@/shared/lib/i18n';
import {type CategoryDetails, type CategoryNode, type CategoryTitles} from '../model/category';
import type {CategoryDraft} from '../model/category-form';

type CategoryDetailsDto = components['schemas']['CategoryDetailsDto'];

function toBody(draft: CategoryDraft): components['schemas']['CategoryRequestDto'] {
    return {titles: draft.titles, parentId: draft.parentId};
}

/** The titles in the languages of the site; one in another language is left out, a category without a Russian one is broken. */
function toTitles(dto: CategoryDetailsDto['titles']): CategoryTitles {
    const titles: Partial<Record<UiLanguage, string>> = {};
    for (const language of SUPPORTED_UI_LANGUAGES) {
        const title = dto[language];
        if (title !== undefined) {
            titles[language] = title;
        }
    }
    const {ru} = dto;
    if (ru === undefined) {
        throw new TypeError('A category has a Russian title.');
    }
    return {...titles, ru};
}

/** The whole tree of categories: `GET /api/categories`. */
export async function fetchCategoryTree(signal: AbortSignal): Promise<readonly CategoryNode[]> {
    const {data, error, response} = await api.GET('/api/categories', {signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** One category with its titles in every language and its parent: `GET /api/categories/{id}`. */
export async function fetchCategory(id: string, signal: AbortSignal): Promise<CategoryDetails> {
    const {data, error, response} = await api.GET('/api/categories/{id}', {params: {path: {id}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return {...data, titles: toTitles(data.titles)};
}

/** Adds a category to the tree and returns its id. */
export async function createCategory(draft: CategoryDraft): Promise<string> {
    const {data, error, response} = await api.POST('/api/categories', {body: toBody(draft)});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data.id;
}

export async function updateCategory(id: string, draft: CategoryDraft): Promise<void> {
    const {error, response} = await api.PUT('/api/categories/{id}', {params: {path: {id}}, body: toBody(draft)});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

/** Deletes the category with its subcategories; their software only leaves them. */
export async function deleteCategory(id: string): Promise<void> {
    const {error, response} = await api.DELETE('/api/categories/{id}', {params: {path: {id}}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}
