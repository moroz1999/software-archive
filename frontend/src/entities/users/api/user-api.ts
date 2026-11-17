import {mapResponse, readBoolean, readObject, readString, readStringArray} from '@/shared/api';
import {type Ability, type CurrentUser, toAbility, toUserGroup, toUserId, type UserEntry, type UserGroup} from '../model/user';

/** The signed-in user in the body of `GET /api/auth/me`; a body it cannot read is a broken response. */
export function readCurrentUser(raw: unknown): CurrentUser {
    return mapResponse(() => {
        const user = readObject(raw, 'current user');
        return {
            id: toUserId(readString(user, 'id')),
            name: readString(user, 'name'),
            email: readString(user, 'email'),
            groups: readStringArray(user['groups'], 'groups').map(toUserGroup).filter((group): group is UserGroup => group !== null),
            abilities: readStringArray(user['abilities'], 'abilities').map(toAbility).filter((ability): ability is Ability => ability !== null),
        };
    });
}

/** An account as an administrator sees it, in the list of users or alone; a body it cannot read is a broken response. */
export function readUserEntry(raw: unknown): UserEntry {
    const dto = readObject(raw, 'user');
    return {
        id: toUserId(readString(dto, 'id')),
        name: readString(dto, 'name'),
        email: readString(dto, 'email'),
        confirmed: readBoolean(dto, 'confirmed'),
        registeredAt: readString(dto, 'registeredAt'),
        groups: readStringArray(dto['groups'], 'groups').map(toUserGroup).filter((group): group is UserGroup => group !== null),
    };
}
