import {deleteResource, getJson, type JsonObject, mapResponse, postJson, postNoContent, putJson, readAddition, readArray, readBoolean, readNullableString, readObject, readString} from '@/shared/api';
import {labelPath} from '../config/paths';
import type {LabelDetails, LabelDraft, LabelLocation, LabelOrigin, LabelOwner, LabelOwnerOption, NamedLabelOwner, NewLabelDraft} from '../model/label';
import {type LabelId, toLabelId} from '../model/label-id';

function readOwnerType(dto: JsonObject, name: string): LabelOwner['type'] {
    const type = readString(dto, name);
    if (type !== 'author' && type !== 'group') {
        throw new TypeError(`Unknown owner type: ${type}`);
    }
    return type;
}

function parseOwner(raw: unknown): NamedLabelOwner | null {
    if (raw === null) {
        return null;
    }
    const dto = readObject(raw, 'owner');
    return {type: readOwnerType(dto, 'type'), id: readString(dto, 'id'), name: readString(dto, 'name'), note: readNullableString(dto, 'note')};
}

function parseLocation(item: unknown): LabelLocation {
    const dto = readObject(item, 'location');
    return {
        countryId: readNullableString(dto, 'countryId'),
        country: readNullableString(dto, 'country'),
        cityId: readNullableString(dto, 'cityId'),
        city: readNullableString(dto, 'city'),
        primary: readBoolean(dto, 'primary'),
    };
}

function parseOrigin(item: unknown): LabelOrigin {
    const dto = readObject(item, 'origin');
    return {origin: readString(dto, 'origin'), externalId: readString(dto, 'externalId'), url: readNullableString(dto, 'url')};
}

/** The page of a label: `GET /api/labels/{id}`. */
export async function fetchLabel(id: LabelId, signal: AbortSignal): Promise<LabelDetails> {
    const json = await getJson(`/api${labelPath(id)}`, signal);
    return mapResponse(() => {
        const dto = readObject(json, 'label');
        return {
            id: toLabelId(readString(dto, 'id')),
            name: readString(dto, 'name'),
            owner: parseOwner(dto['owner']),
            groups: readArray(dto['groups'], 'groups').map((item) => {
                const group = readObject(item, 'group');
                return {id: readString(group, 'id'), name: readString(group, 'name')};
            }),
            locations: readArray(dto['locations'], 'locations').map(parseLocation),
            origins: readArray(dto['origins'], 'origins').map(parseOrigin),
            added: readAddition(dto['added']),
            canChange: readBoolean(dto, 'canChange'),
            canMakeOwner: readBoolean(dto, 'canMakeOwner'),
            canEdit: readBoolean(dto, 'canEdit'),
            canChangeOwner: readBoolean(dto, 'canChangeOwner'),
            canGiveToGroup: readBoolean(dto, 'canGiveToGroup'),
        };
    });
}

/** Adds a label of nobody with its locations and returns its id: `POST /api/labels`. */
export async function createLabel(draft: NewLabelDraft): Promise<LabelId> {
    const json = await postJson('/api/labels', {
        name: draft.name,
        locations: draft.locations.map((location) => ({country: location.country, city: location.city, primary: location.primary})),
    });
    return mapResponse(() => toLabelId(readString(readObject(json, 'created label'), 'id')));
}

/** Gives the label its name, whose it is and its locations: `PUT /api/labels/{id}`. */
export async function updateLabel(id: LabelId, draft: LabelDraft): Promise<void> {
    await putJson(`/api${labelPath(id)}`, {
        name: draft.name,
        owner: draft.owner === null ? null : {type: draft.owner.type, id: draft.owner.id, display: draft.owner.display},
        locations: draft.locations.map((location) => ({country: location.country, city: location.city, primary: location.primary})),
    });
}

/** Deletes the label with its credits and memberships: `DELETE /api/labels/{id}`. */
export async function deleteLabel(id: LabelId): Promise<void> {
    await deleteResource(`/api${labelPath(id)}`);
}

/** Attaches a label of nobody to its owner, as the owner's display name when told to: `POST /api/labels/{id}/owner`. */
export async function attachLabel(id: LabelId, owner: LabelOwner, display: boolean): Promise<void> {
    await postNoContent(`/api${labelPath(id)}/owner`, {type: owner.type, id: owner.id, display});
}

/** Replaces the label with a new person of the same name: `POST /api/labels/{id}/author`. */
export async function makeAuthorOfLabel(id: LabelId): Promise<LabelOwner> {
    const json = await postJson(`/api${labelPath(id)}/author`, {});
    return mapResponse(() => ({type: 'author', id: readString(readObject(json, 'made person'), 'id')}));
}

/** Makes a new group of the types under the name of the label, taking it from whoever had it: `POST /api/labels/{id}/group`. */
export async function makeGroupOfLabel(id: LabelId, types: readonly string[]): Promise<LabelOwner> {
    const json = await postJson(`/api${labelPath(id)}/group`, {types});
    return mapResponse(() => ({type: 'group', id: readString(readObject(json, 'made group'), 'id')}));
}

/** Merges the label into the group it is a duplicate of, the label deleted: `POST /api/labels/{id}/merge-into-group`. */
export async function mergeLabelIntoGroup(id: LabelId, group: string): Promise<void> {
    await postNoContent(`/api${labelPath(id)}/merge-into-group`, {group});
}

/** Makes the label a label of the group, taken from another group that has it: `POST /api/labels/{id}/attach-to-group`. */
export async function attachLabelToGroup(id: LabelId, group: string): Promise<void> {
    await postNoContent(`/api${labelPath(id)}/attach-to-group`, {group});
}

/** Groups by name, for giving a label to one of them. */
export async function fetchLabelGroups(query: string, signal: AbortSignal): Promise<readonly LabelOwnerOption[]> {
    const params = new URLSearchParams({types: 'group', query});
    const json = await getJson(`/api/entity-lookup?${params.toString()}`, signal);
    return mapResponse(() => readArray(readObject(json, 'group lookup')['items'], 'items').map((item) => {
        const dto = readObject(item, 'group');
        return {type: readOwnerType(dto, 'type'), id: readString(dto, 'id'), title: readString(dto, 'title')};
    }));
}

/** People and groups by name, for attaching a label to one of them. */
export async function fetchLabelOwners(query: string, signal: AbortSignal): Promise<readonly LabelOwnerOption[]> {
    const params = new URLSearchParams({types: 'author,group', query});
    const json = await getJson(`/api/entity-lookup?${params.toString()}`, signal);
    return mapResponse(() => readArray(readObject(json, 'owner lookup')['items'], 'items').map((item) => {
        const dto = readObject(item, 'owner');
        return {type: readOwnerType(dto, 'type'), id: readString(dto, 'id'), title: readString(dto, 'title')};
    }));
}
