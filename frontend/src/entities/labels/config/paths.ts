
/** Adding a label of nobody. */
export const LABEL_NEW_PATH = '/labels/new';

/** The route pattern of a label's page; its parameter is `id`. */
export const LABEL_ROUTE = '/labels/:id';

export function labelPath(id: string): string {
    return `/labels/${id}`;
}

/** The route pattern of editing a label; its parameter is `id`. */
export const LABEL_EDIT_ROUTE = '/labels/:id/edit';

export function labelEditPath(id: string): string {
    return `/labels/${id}/edit`;
}
