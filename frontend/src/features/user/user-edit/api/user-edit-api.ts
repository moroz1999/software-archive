import {readUserEntry, type UserEntry, type UserId} from '@/entities/users';
import {getJson, mapResponse, putJson} from '@/shared/api';
import type {UserDraft} from '../model/user-form';

function userPath(id: UserId): string {
    return `/api/users/${id}`;
}

/** One account as an administrator keeps it: `GET /api/users/{id}`. */
export async function fetchUser(id: UserId, signal: AbortSignal): Promise<UserEntry> {
    const json = await getJson(userPath(id), signal);
    return mapResponse(() => readUserEntry(json));
}

/** Gives the account its name, email and groups: `PUT /api/users/{id}`. */
export async function updateUser(id: UserId, draft: UserDraft): Promise<void> {
    await putJson(userPath(id), {name: draft.name, email: draft.email, groups: draft.groups});
}
