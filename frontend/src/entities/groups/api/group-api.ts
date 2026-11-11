import {
    deleteResource,
    getJson,
    mapResponse,
    postJson,
    putJson,
    readArray,
    readBoolean,
    readNullableString,
    readObject,
    readString,
    readStringArray,
} from '@/shared/api';
import {type GroupDetails, type GroupId, type GroupLabel, type GroupLocation, type GroupMember, type RelatedGroup, toGroupId} from '../model/group';
import type {GroupDraft} from '../model/group-form';

function groupPath(id: GroupId): string {
    return `/api/groups/${id}`;
}

function toBody(draft: GroupDraft): unknown {
    return {
        name: draft.name,
        notes: draft.notes,
        types: draft.types,
        labels: draft.labels.map((label) => ({id: label.id, name: label.name, display: label.display})),
        locations: draft.locations.map((location) => ({country: location.country, city: location.city, primary: location.primary})),
    };
}

function parseLabel(item: unknown): GroupLabel {
    const dto = readObject(item, 'label');
    return {id: readString(dto, 'id'), name: readString(dto, 'name'), display: readBoolean(dto, 'display')};
}

function parseLocation(item: unknown): GroupLocation {
    const dto = readObject(item, 'location');
    return {
        countryId: readNullableString(dto, 'countryId'),
        country: readNullableString(dto, 'country'),
        cityId: readNullableString(dto, 'cityId'),
        city: readNullableString(dto, 'city'),
        primary: readBoolean(dto, 'primary'),
    };
}

function parseMember(item: unknown): GroupMember {
    const dto = readObject(item, 'member');
    return {name: readString(dto, 'name'), personId: readNullableString(dto, 'personId'), personName: readNullableString(dto, 'personName')};
}

function parseRelated(item: unknown): RelatedGroup {
    const dto = readObject(item, 'group');
    return {id: toGroupId(readString(dto, 'id')), name: readString(dto, 'name')};
}

/** One group with its details: `GET /api/groups/{id}`. */
export async function fetchGroup(id: GroupId, signal: AbortSignal): Promise<GroupDetails> {
    const json = await getJson(groupPath(id), signal);
    return mapResponse(() => {
        const dto = readObject(json, 'group');
        return {
            id: toGroupId(readString(dto, 'id')),
            name: readString(dto, 'name'),
            groupName: readString(dto, 'groupName'),
            notes: readNullableString(dto, 'notes'),
            types: readStringArray(dto['types'], 'types'),
            labels: readArray(dto['labels'], 'labels').map(parseLabel),
            locations: readArray(dto['locations'], 'locations').map(parseLocation),
            members: readArray(dto['members'], 'members').map(parseMember),
            memberGroups: readArray(dto['memberGroups'], 'memberGroups').map(parseRelated),
            groups: readArray(dto['groups'], 'groups').map(parseRelated),
        };
    });
}

/** Adds a group to the archive and returns its id. */
export async function createGroup(draft: GroupDraft): Promise<GroupId> {
    const json = await postJson('/api/groups', toBody(draft));
    return mapResponse(() => toGroupId(readString(readObject(json, 'created group'), 'id')));
}

export async function updateGroup(id: GroupId, draft: GroupDraft): Promise<void> {
    await putJson(groupPath(id), toBody(draft));
}

/** Deletes the group with its memberships; its labels stay in the archive as labels of nobody. */
export async function deleteGroup(id: GroupId): Promise<void> {
    await deleteResource(groupPath(id));
}

/** The codes of the reference book of group types: `GET /api/group-types`. */
export async function fetchGroupTypes(signal: AbortSignal): Promise<readonly string[]> {
    const json = await getJson('/api/group-types', signal);
    return mapResponse(() => readStringArray(json, 'group types'));
}
