import type {SoftwareId} from '../model/software';

/** Where software lives in the app. */
export const SOFTWARE_LIST_PATH = '/';
export const SOFTWARE_NEW_PATH = '/software/new';
/** The route pattern of the software's page; its parameter is `id`. */
export const SOFTWARE_ROUTE = '/software/:id';
/** The route pattern of the edit page; its parameter is `id`. */
export const SOFTWARE_EDIT_ROUTE = '/software/:id/edit';

export function softwarePath(id: SoftwareId): string {
    return `/software/${id}`;
}

export function softwareEditPath(id: SoftwareId): string {
    return `/software/${id}/edit`;
}
