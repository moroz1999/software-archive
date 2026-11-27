import type {UserEntry} from '@/entities/users';
import {api, apiError} from '@/shared/api';
import type {UserDraft} from '../model/user-form';

/** One account as an administrator keeps it: `GET /api/users/{id}`. */
export async function fetchUser(id: string, signal: AbortSignal): Promise<UserEntry> {
    const {data, error, response} = await api.GET('/api/users/{id}', {params: {path: {id}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** Gives the account its name, email and groups: `PUT /api/users/{id}`. */
export async function updateUser(id: string, draft: UserDraft): Promise<void> {
    const {error, response} = await api.PUT('/api/users/{id}', {params: {path: {id}}, body: {name: draft.name, email: draft.email, groups: draft.groups}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}
