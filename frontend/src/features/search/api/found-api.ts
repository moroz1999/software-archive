import {toAuthorId} from '@/entities/authors';
import {toGroupId} from '@/entities/groups';
import {readNullableString, readObject, readString} from '@/shared/api';
import type {FoundAuthor, FoundGroup} from '../model/found';

/** A person found by the search, as the API sends it. */
export function parseFoundAuthor(item: unknown): FoundAuthor {
    const author = readObject(item, 'author');
    return {id: toAuthorId(readString(author, 'id')), name: readString(author, 'name'), realName: readNullableString(author, 'realName')};
}

/** A group found by the search, as the API sends it. */
export function parseFoundGroup(item: unknown): FoundGroup {
    const group = readObject(item, 'group');
    return {id: toGroupId(readString(group, 'id')), name: readString(group, 'name')};
}
