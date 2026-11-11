import {deleteResource, getJson, mapResponse, postJson, putJson, readArray, readNullableString, readObject, readString} from '@/shared/api';
import {type CategoryDetails, type CategoryId, type CategoryNode, toCategoryId} from '../model/category';
import type {CategoryDraft} from '../model/category-form';

function categoryPath(id: CategoryId): string {
    return `/api/categories/${id}`;
}

function toBody(draft: CategoryDraft): unknown {
    return {title: draft.title, parentId: draft.parentId};
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

/** One category with its parent: `GET /api/categories/{id}`. */
export async function fetchCategory(id: CategoryId, signal: AbortSignal): Promise<CategoryDetails> {
    const json = await getJson(categoryPath(id), signal);
    return mapResponse(() => {
        const dto = readObject(json, 'category');
        const parentId = readNullableString(dto, 'parentId');
        return {
            id: toCategoryId(readString(dto, 'id')),
            title: readString(dto, 'title'),
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
