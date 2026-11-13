import type {CategoryId} from '../model/category';

/** Where categories live in the app. */
export const CATEGORIES_PATH = '/management/categories';
export const CATEGORY_NEW_PATH = '/management/categories/new';
/** The route pattern of the edit page; its parameter is `id`. */
export const CATEGORY_EDIT_ROUTE = '/management/categories/:id/edit';

export function categoryEditPath(id: CategoryId): string {
    return `/management/categories/${id}/edit`;
}
