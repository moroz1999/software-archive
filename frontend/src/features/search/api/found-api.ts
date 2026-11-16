import {toAuthorId} from '@/entities/authors';
import {toGroupId} from '@/entities/groups';
import {toLabelId} from '@/entities/labels';
import {readNullableString, readObject, readString, SystemError} from '@/shared/api';
import type {FoundAuthor, FoundGroup} from '../model/found';

/** A person or a label of nobody found by the search, as the API sends it. */
export function parseFoundAuthor(item: unknown): FoundAuthor {
    const author = readObject(item, 'author');
    const kind = readString(author, 'kind');
    switch (kind) {
        case 'person':
            return {kind, id: toAuthorId(readString(author, 'id')), name: readString(author, 'name'), realName: readNullableString(author, 'realName')};
        case 'label':
            return {kind, id: toLabelId(readString(author, 'id')), name: readString(author, 'name')};
        default:
            throw new SystemError('response', `Unknown kind of a found author: ${kind}`);
    }
}

/** A group found by the search, as the API sends it. */
export function parseFoundGroup(item: unknown): FoundGroup {
    const group = readObject(item, 'group');
    return {id: toGroupId(readString(group, 'id')), name: readString(group, 'name')};
}
