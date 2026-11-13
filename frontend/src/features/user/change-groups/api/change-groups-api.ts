import type {GrantedGroup, UserId} from '@/entities/users';
import {putJson} from '@/shared/api';

/** Gives the user the groups: `PUT /api/users/{id}/groups`. */
export function changeUserGroups(id: UserId, groups: readonly GrantedGroup[]): Promise<void> {
    return putJson(`/api/users/${id}/groups`, {groups});
}
