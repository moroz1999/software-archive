import {getJson, mapResponse, postJson, postNoContent, readArray, readBoolean, readNullableString, readObject, readString, type JsonObject} from '@/shared/api';
import {labelPath} from '../config/paths';
import type {LabelDetails, LabelOwner, LabelOwnerOption, NamedLabelOwner} from '../model/label';
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
            canChange: readBoolean(dto, 'canChange'),
            canMakeOwner: readBoolean(dto, 'canMakeOwner'),
        };
    });
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

/** People and groups by name, for attaching a label to one of them. */
export async function fetchLabelOwners(query: string, signal: AbortSignal): Promise<readonly LabelOwnerOption[]> {
    const params = new URLSearchParams({types: 'author,group', query});
    const json = await getJson(`/api/entity-lookup?${params.toString()}`, signal);
    return mapResponse(() => readArray(readObject(json, 'owner lookup')['items'], 'items').map((item) => {
        const dto = readObject(item, 'owner');
        return {type: readOwnerType(dto, 'type'), id: readString(dto, 'id'), title: readString(dto, 'title')};
    }));
}
