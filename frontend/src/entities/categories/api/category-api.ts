import {deleteResource, getJson, mapResponse, postJson, putJson, readArray, readNullableString, readObject, readString} from '@/shared/api';
import {SUPPORTED_UI_LANGUAGES, type UiLanguage} from '@/shared/lib/i18n';
import {type CategoryDetails, type CategoryId, type CategoryNode, type CategoryTitles, toCategoryId} from '../model/category';
import type {CategoryDraft} from '../model/category-form';

function categoryPath(id: CategoryId): string {
    return `/api/categories/${id}`;
}

function toBody(draft: CategoryDraft): unknown {
    return {titles: draft.titles, parentId: draft.parentId};
}

/** The titles in the languages of the site; one in another language is left out, a missing Russian one is malformed. */
function parseTitles(value: unknown): CategoryTitles {
    const dto = readObject(value, 'titles');
    const titles: Partial<Record<UiLanguage, string>> = {};
    for (const language of SUPPORTED_UI_LANGUAGES) {
        if (dto[language] !== undefined) {
            titles[language] = readString(dto, language);
        }
    }
    return {...titles, ru: readString(dto, 'ru')};
}

function parseNode(item: unknown): CategoryNode {
    const dto = readObject(item, 'category');
    return {
        id: toCategoryId(readString(dto, 'id')),
        title: readString(dto, 'title'),
        children: readArray(dto['children'], 'children').map(parseNode),
    };
}

/** The whole tree of categories: `GET /api/categories`. */
export async function fetchCategoryTree(signal: AbortSignal): Promise<readonly CategoryNode[]> {
    const json = await getJson('/api/categories', signal);
    return mapResponse(() => readArray(json, 'categories').map(parseNode));
}

/** One category with its titles in every language and its parent: `GET /api/categories/{id}`. */
export async function fetchCategory(id: CategoryId, signal: AbortSignal): Promise<CategoryDetails> {
    const json = await getJson(categoryPath(id), signal);
    return mapResponse(() => {
        const dto = readObject(json, 'category');
        const parentId = readNullableString(dto, 'parentId');
        return {
            id: toCategoryId(readString(dto, 'id')),
            titles: parseTitles(dto['titles']),
            parentId: parentId === null ? null : toCategoryId(parentId),
        };
    });
}

/** Adds a category to the tree and returns its id. */
export async function createCategory(draft: CategoryDraft): Promise<CategoryId> {
    const json = await postJson('/api/categories', toBody(draft));
    return mapResponse(() => toCategoryId(readString(readObject(json, 'created category'), 'id')));
}

export async function updateCategory(id: CategoryId, draft: CategoryDraft): Promise<void> {
    await putJson(categoryPath(id), toBody(draft));
}

/** Deletes the category with its subcategories; their software only leaves them. */
export async function deleteCategory(id: CategoryId): Promise<void> {
    await deleteResource(categoryPath(id));
}
