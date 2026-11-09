import {getJson, mapResponse, readArray, readBoolean, readNullableString, readObject, readString} from '@/shared/api';
import {type AuthorAlias, type AuthorDetails, type AuthorGroup, type AuthorId, type AuthorLocation, toAuthorId} from '../model/author';

function parseNamed(item: unknown, what: string): AuthorAlias | AuthorGroup {
    const dto = readObject(item, what);
    return {id: readString(dto, 'id'), name: readString(dto, 'name')};
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
            aliases: readArray(dto['aliases'], 'aliases').map((item) => parseNamed(item, 'alias')),
            locations: readArray(dto['locations'], 'locations').map(parseLocation),
            groups: readArray(dto['groups'], 'groups').map((item) => parseNamed(item, 'group')),
        };
    });
}
