import {mapResponse, readObject, readString, readStringArray} from '@/shared/api';
import {type Ability, type CurrentUser, toAbility, toUserGroup, toUserId, type UserGroup} from '../model/user';

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
