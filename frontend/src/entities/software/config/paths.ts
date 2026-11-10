import type {ReleaseId} from '../model/release';
import type {SoftwareId} from '../model/software';

/** Where software lives in the app. */
export const SOFTWARE_LIST_PATH = '/software';
export const SOFTWARE_NEW_PATH = '/software/new';
/** The route pattern of the software's page; its parameter is `id`. */
export const SOFTWARE_ROUTE = '/software/:id';
/** The route pattern of the edit page; its parameter is `id`. */
export const SOFTWARE_EDIT_ROUTE = '/software/:id/edit';

/** The route pattern of adding a release; its parameter is `id`, of the software. */
export const RELEASE_NEW_ROUTE = '/software/:id/releases/new';
/** The route pattern of editing a release; its parameters are `id`, of the software, and `releaseId`. */
export const RELEASE_EDIT_ROUTE = '/software/:id/releases/:releaseId/edit';

export function softwarePath(id: SoftwareId): string {
    return `/software/${id}`;
}

export function softwareEditPath(id: SoftwareId): string {
    return `/software/${id}/edit`;
}

export function releaseNewPath(id: SoftwareId): string {
    return `/software/${id}/releases/new`;
}

export function releaseEditPath(id: SoftwareId, releaseId: ReleaseId): string {
    return `/software/${id}/releases/${releaseId}/edit`;
}
