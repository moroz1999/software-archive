import type {CategoryId} from '../model/category';

/** Where categories live in the app. */
export const CATEGORIES_PATH = '/categories';
export const CATEGORY_NEW_PATH = '/categories/new';
/** The route pattern of the edit page; its parameter is `id`. */
export const CATEGORY_EDIT_ROUTE = '/categories/:id/edit';

export function categoryEditPath(id: CategoryId): string {
    return `/categories/${id}/edit`;
}
