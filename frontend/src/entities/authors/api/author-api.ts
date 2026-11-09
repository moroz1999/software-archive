import {getJson, mapResponse, readArray, readBoolean, readNullableString, readObject, readString} from '@/shared/api';
import {type AuthorDetails, type AuthorGroup, type AuthorId, type AuthorLabel, type AuthorLocation, toAuthorId} from '../model/author';

function parseLabel(item: unknown): AuthorLabel {
    const dto = readObject(item, 'label');
    return {id: readString(dto, 'id'), name: readString(dto, 'name')};
}

function parseGroup(item: unknown): AuthorGroup {
    const dto = readObject(item, 'group');
    return {id: readString(dto, 'id'), name: readString(dto, 'name'), as: readNullableString(dto, 'as')};
}

function parseLocation(item: unknown): AuthorLocation {
    const dto = readObject(item, 'location');
    return {country: readNullableString(dto, 'country'), city: readNullableString(dto, 'city'), primary: readBoolean(dto, 'primary')};
}

/** One author with their details: `GET /api/authors/{id}`. */
export async function fetchAuthor(id: AuthorId, signal: AbortSignal): Promise<AuthorDetails> {
    const json = await getJson(`/api/authors/${id}`, signal);
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
