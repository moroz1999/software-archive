import {toUserGroup, toUserId, type UserEntry, type UserGroup} from '@/entities/users';
import {getJson, mapResponse, type Page, parsePage, readBoolean, readObject, readString, readStringArray} from '@/shared/api';
import type {UserListFilter} from '../model/user-list-filter';

/** Users on one page of the list. */
const PAGE_SIZE = 50;

function parseEntry(item: unknown): UserEntry {
    const dto = readObject(item, 'user');
    return {
        id: toUserId(readString(dto, 'id')),
        name: readString(dto, 'name'),
        email: readString(dto, 'email'),
        confirmed: readBoolean(dto, 'confirmed'),
        registeredAt: readString(dto, 'registeredAt'),
        groups: readStringArray(dto['groups'], 'groups').map(toUserGroup).filter((group): group is UserGroup => group !== null),
    };
}

/** A page of the users: `GET /api/users`, with the search when one is typed. */
export async function fetchUserPage(filter: UserListFilter, signal: AbortSignal): Promise<Page<UserEntry>> {
    const search = new URLSearchParams({page: String(filter.page), size: String(PAGE_SIZE)});
    if (filter.query !== null) {
        search.set('query', filter.query);
    }
    const json = await getJson(`/api/users?${search.toString()}`, signal);
    return mapResponse(() => parsePage(json, parseEntry));
}
