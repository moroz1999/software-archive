import type {LabelId} from '../model/label-id';

/** The route pattern of a label's page; its parameter is `id`. */
export const LABEL_ROUTE = '/labels/:id';

export function labelPath(id: LabelId): string {
    return `/labels/${id}`;
}

/** The route pattern of editing a label; its parameter is `id`. */
export const LABEL_EDIT_ROUTE = '/labels/:id/edit';

export function labelEditPath(id: LabelId): string {
    return `/labels/${id}/edit`;
}
