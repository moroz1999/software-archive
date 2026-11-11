import type {AuthorId} from '../model/author';

/** Where authors live in the app. */
export const AUTHORS_PATH = '/authors';
export const AUTHOR_NEW_PATH = '/authors/new';
/** The route pattern of an author's page; its parameter is `id`. */
export const AUTHOR_ROUTE = '/authors/:id';
/** The route pattern of the edit page; its parameter is `id`. */
export const AUTHOR_EDIT_ROUTE = '/authors/:id/edit';

export function authorPath(id: AuthorId): string {
    return `/authors/${id}`;
}

export function authorEditPath(id: AuthorId): string {
    return `/authors/${id}/edit`;
}
