import {api, apiError, type components} from '@/shared/api';
import {type GroupDetails} from '../model/group';
import type {GroupDraft, GroupPerson} from '../model/group-form';

function toBody(draft: GroupDraft): components['schemas']['GroupRequestDto'] {
    return {
        name: draft.name,
        notes: draft.notes,
        types: draft.types,
        labels: draft.labels.map((label) => ({id: label.id, name: label.name})),
        locations: draft.locations.map((location) => ({country: location.country, city: location.city, primary: location.primary})),
        members: (draft.members ?? []).map(({id, type, roles}) => ({id, type, roles})),
    };
}

/** One group with its details: `GET /api/groups/{id}`. */
export async function fetchGroup(id: string, signal: AbortSignal): Promise<GroupDetails> {
    const {data, error, response} = await api.GET('/api/groups/{id}', {params: {path: {id}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** Adds a group to the archive and returns its id. */
export async function createGroup(draft: GroupDraft): Promise<string> {
    const {data, error, response} = await api.POST('/api/groups', {body: toBody(draft)});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data.id;
}

export async function updateGroup(id: string, draft: GroupDraft): Promise<void> {
    const {error, response} = await api.PUT('/api/groups/{id}', {params: {path: {id}}, body: toBody(draft)});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

/** Deletes the group with its memberships; its labels stay in the archive as labels of nobody. */
export async function deleteGroup(id: string): Promise<void> {
    const {error, response} = await api.DELETE('/api/groups/{id}', {params: {path: {id}}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

/** The codes of the reference book of group types: `GET /api/group-types`. */
export async function fetchGroupTypes(signal: AbortSignal): Promise<readonly string[]> {
    const {data, error, response} = await api.GET('/api/group-types', {signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** Finds people and their aliases for the group roster. */
export async function fetchGroupPeople(query: string, signal: AbortSignal): Promise<readonly GroupPerson[]> {
    const {data, error, response} = await api.GET('/api/entity-lookup', {params: {query: {types: 'author,author-label', query}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    // The lookup answers only the asked kinds; the check tells the compiler so.
    return data.items.flatMap(({id, type, title}) => type === 'author' || type === 'author-label' ? [{id, type, title}] : []);
}
