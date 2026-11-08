import type {SoftwareId} from '../model/software';

/** Where software lives in the app. */
export const SOFTWARE_LIST_PATH = '/';
export const SOFTWARE_NEW_PATH = '/software/new';
/** The route pattern of the edit page; its parameter is `id`. */
export const SOFTWARE_EDIT_ROUTE = '/software/:id/edit';

export function softwareEditPath(id: SoftwareId): string {
    return `/software/${id}/edit`;
}
