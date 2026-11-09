import type {AuthorId} from '../model/author';

/** Where authors live in the app. */
export const AUTHORS_PATH = '/authors';
/** The route pattern of an author's page; its parameter is `id`. */
export const AUTHOR_ROUTE = '/authors/:id';

export function authorPath(id: AuthorId): string {
    return `/authors/${id}`;
}
