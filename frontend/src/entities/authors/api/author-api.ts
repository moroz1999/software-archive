import {deleteResource, getJson, mapResponse, postJson, putJson, readArray, readBoolean, readNullableString, readObject, readString} from '@/shared/api';
import {type AuthorDetails, type AuthorGroup, type AuthorId, type AuthorLabel, type AuthorLocation, toAuthorId} from '../model/author';
import type {AuthorDraft} from '../model/author-form';

function authorPath(id: AuthorId): string {
    return `/api/authors/${id}`;
}

function toBody(draft: AuthorDraft): unknown {
    return {
        realName: draft.realName,
        notes: draft.notes,
        labels: draft.labels.map((label) => ({id: label.id, name: label.name, display: label.display})),
        locations: draft.locations.map((location) => ({country: location.country, city: location.city, primary: location.primary})),
    };
}

function parseLabel(item: unknown): AuthorLabel {
    const dto = readObject(item, 'label');
    return {id: readString(dto, 'id'), name: readString(dto, 'name'), display: readBoolean(dto, 'display')};
}

function parseGroup(item: unknown): AuthorGroup {
    const dto = readObject(item, 'group');
    return {id: readString(dto, 'id'), name: readString(dto, 'name'), as: readNullableString(dto, 'as')};
}

function parseLocation(item: unknown): AuthorLocation {
    const dto = readObject(item, 'location');
    return {
        countryId: readNullableString(dto, 'countryId'),
        country: readNullableString(dto, 'country'),
        cityId: readNullableString(dto, 'cityId'),
        city: readNullableString(dto, 'city'),
        primary: readBoolean(dto, 'primary'),
    };
}

/** One author with their details: `GET /api/authors/{id}`. */
export async function fetchAuthor(id: AuthorId, signal: AbortSignal): Promise<AuthorDetails> {
    const json = await getJson(authorPath(id), signal);
    return mapResponse(() => {
        const dto = readObject(json, 'author');
        return {
            id: toAuthorId(readString(dto, 'id')),
            name: readString(dto, 'name'),
            realName: readNullableString(dto, 'realName'),
            notes: readNullableString(dto, 'notes'),
            labels: readArray(dto['labels'], 'labels').map(parseLabel),
            locations: readArray(dto['locations'], 'locations').map(parseLocation),
            groups: readArray(dto['groups'], 'groups').map(parseGroup),
        };
    });
}

/** Adds an author to the archive and returns their id. */
export async function createAuthor(draft: AuthorDraft): Promise<AuthorId> {
    const json = await postJson('/api/authors', toBody(draft));
    return mapResponse(() => toAuthorId(readString(readObject(json, 'created author'), 'id')));
}

export async function updateAuthor(id: AuthorId, draft: AuthorDraft): Promise<void> {
    await putJson(authorPath(id), toBody(draft));
}

/** Deletes the person; their labels stay in the archive as labels of nobody. */
export async function deleteAuthor(id: AuthorId): Promise<void> {
    await deleteResource(authorPath(id));
}
