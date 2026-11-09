import {getJson, mapResponse, readArray, readBoolean, readNullableString, readObject, readString, readStringArray} from '@/shared/api';
import {type GroupDetails, type GroupId, type GroupLabel, type GroupLocation, type GroupMember, type RelatedGroup, toGroupId} from '../model/group';

function parseLabel(item: unknown): GroupLabel {
    const dto = readObject(item, 'label');
    return {id: readString(dto, 'id'), name: readString(dto, 'name')};
}

function parseLocation(item: unknown): GroupLocation {
    const dto = readObject(item, 'location');
    return {country: readNullableString(dto, 'country'), city: readNullableString(dto, 'city'), primary: readBoolean(dto, 'primary')};
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
    const json = await getJson(`/api/groups/${id}`, signal);
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
