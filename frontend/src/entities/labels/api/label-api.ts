import {api, apiError, type components} from '@/shared/api';
import type {EntityAutocompleteOption} from '@/shared/ui/multi-entity-autocomplete';
import type {LabelDetails, LabelDraft, LabelOwner, LabelOwnerOption, NewLabelDraft} from '../model/label';

function toLocations(locations: NewLabelDraft['locations']): components['schemas']['LabelRequestDto']['locations'] {
    return locations.map((location) => ({country: location.country, city: location.city, primary: location.primary}));
}

/** The page of a label: `GET /api/labels/{id}`. */
export async function fetchLabel(id: string, signal: AbortSignal): Promise<LabelDetails> {
    const {data, error, response} = await api.GET('/api/labels/{id}', {params: {path: {id}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** Adds a label of nobody with its locations and returns its id: `POST /api/labels`. */
export async function createLabel(draft: NewLabelDraft): Promise<string> {
    const {data, error, response} = await api.POST('/api/labels', {body: {name: draft.name, locations: toLocations(draft.locations)}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data.id;
}

/** Gives the label its name, whose it is and its locations: `PUT /api/labels/{id}`. */
export async function updateLabel(id: string, draft: LabelDraft): Promise<void> {
    const {error, response} = await api.PUT('/api/labels/{id}', {
        params: {path: {id}},
        body: {
            name: draft.name,
            owner: draft.owner === null ? null : {type: draft.owner.type, id: draft.owner.id, display: draft.owner.display},
            locations: toLocations(draft.locations),
        },
    });
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

/** Deletes the label with its credits and memberships: `DELETE /api/labels/{id}`. */
export async function deleteLabel(id: string): Promise<void> {
    const {error, response} = await api.DELETE('/api/labels/{id}', {params: {path: {id}}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

/** Attaches a label of nobody to its owner, as the owner's display name when told to: `POST /api/labels/{id}/owner`. */
export async function attachLabel(id: string, owner: LabelOwner, display: boolean): Promise<void> {
    const {error, response} = await api.POST('/api/labels/{id}/owner', {params: {path: {id}}, body: {type: owner.type, id: owner.id, display}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

/** Replaces the label with a new person of the same name: `POST /api/labels/{id}/author`. */
export async function makeAuthorOfLabel(id: string): Promise<LabelOwner> {
    const {data, error, response} = await api.POST('/api/labels/{id}/author', {params: {path: {id}}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return {type: 'author', id: data.id};
}

/** Makes a new group of the types under the name of the label, taking it from whoever had it: `POST /api/labels/{id}/group`. */
export async function makeGroupOfLabel(id: string, types: readonly string[]): Promise<LabelOwner> {
    const {data, error, response} = await api.POST('/api/labels/{id}/group', {params: {path: {id}}, body: {types}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return {type: 'group', id: data.id};
}

/** Merges the label into the group it is a duplicate of, the label deleted: `POST /api/labels/{id}/merge-into-group`. */
export async function mergeLabelIntoGroup(id: string, group: string): Promise<void> {
    const {error, response} = await api.POST('/api/labels/{id}/merge-into-group', {params: {path: {id}}, body: {group}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

/** Merges the label into the person it is a duplicate of, the label deleted: `POST /api/labels/{id}/merge-into-author`. */
export async function mergeLabelIntoAuthor(id: string, author: string): Promise<void> {
    const {error, response} = await api.POST('/api/labels/{id}/merge-into-author', {params: {path: {id}}, body: {author}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

/** Merges the label into another label it is a duplicate of, the label deleted: `POST /api/labels/{id}/merge-into-label`. */
export async function mergeLabelIntoLabel(id: string, label: string): Promise<void> {
    const {error, response} = await api.POST('/api/labels/{id}/merge-into-label', {params: {path: {id}}, body: {label}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

/** Makes the label a label of the group, taken from another group that has it: `POST /api/labels/{id}/attach-to-group`. */
export async function attachLabelToGroup(id: string, group: string): Promise<void> {
    const {error, response} = await api.POST('/api/labels/{id}/attach-to-group', {params: {path: {id}}, body: {group}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

async function lookup(types: string, query: string, signal: AbortSignal): Promise<readonly components['schemas']['SoftwareReferenceDto'][]> {
    const {data, error, response} = await api.GET('/api/entity-lookup', {params: {query: {types, query}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data.items;
}

/** The people and groups found; the lookup answers only the asked kinds, the check tells the compiler so. */
function owners(found: readonly components['schemas']['SoftwareReferenceDto'][]): readonly LabelOwnerOption[] {
    return found.flatMap(({type, id, title}) => type === 'author' || type === 'group' ? [{type, id, title}] : []);
}

/** Groups by name, for giving a label to one of them. */
export async function fetchLabelGroups(query: string, signal: AbortSignal): Promise<readonly LabelOwnerOption[]> {
    return owners(await lookup('group', query, signal));
}

/** People by name, for merging a label into one of them. */
export async function fetchLabelAuthors(query: string, signal: AbortSignal): Promise<readonly EntityAutocompleteOption[]> {
    return lookup('author', query, signal);
}

/** Labels by name, of nobody, of people and of groups, for merging a label into one of them. */
export async function fetchLabelsByName(query: string, signal: AbortSignal): Promise<readonly EntityAutocompleteOption[]> {
    return lookup('author-label,group-label', query, signal);
}

/** People and groups by name, for attaching a label to one of them. */
export async function fetchLabelOwners(query: string, signal: AbortSignal): Promise<readonly LabelOwnerOption[]> {
    return owners(await lookup('author,group', query, signal));
}
