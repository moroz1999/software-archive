import type {GroupId} from '../model/group';

/** Where groups live in the app. */
export const GROUPS_PATH = '/groups';
/** The route pattern of a group's page; its parameter is `id`. */
export const GROUP_ROUTE = '/groups/:id';

export function groupPath(id: GroupId): string {
    return `/groups/${id}`;
}
