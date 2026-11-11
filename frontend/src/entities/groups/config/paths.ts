import type {GroupId} from '../model/group';

/** Where groups live in the app. */
export const GROUPS_PATH = '/groups';
export const GROUP_NEW_PATH = '/groups/new';
/** The route pattern of a group's page; its parameter is `id`. */
export const GROUP_ROUTE = '/groups/:id';
/** The route pattern of the edit page; its parameter is `id`. */
export const GROUP_EDIT_ROUTE = '/groups/:id/edit';

export function groupPath(id: GroupId): string {
    return `/groups/${id}`;
}

export function groupEditPath(id: GroupId): string {
    return `/groups/${id}/edit`;
}
