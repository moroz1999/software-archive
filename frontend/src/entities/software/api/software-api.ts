import {api, apiError, type components, softwareReferenceTypeValues} from '@/shared/api';
import {type SoftwareRelease} from '../model/release';
import {type SoftwareDetails, type SoftwareProgram, type SoftwareReference, type SoftwareReferenceType} from '../model/software';
import type {SoftwareDraft} from '../model/software-form';
import type {EntityAutocompleteOption} from '@/shared/ui/multi-entity-autocomplete';

function toBody(draft: SoftwareDraft): components['schemas']['SoftwareRequestDto'] {
    return {
        title: draft.title,
        description: draft.description,
        releaseDate: draft.releaseDate,
        platform: draft.platform,
        hardware: draft.hardware,
        categories: draft.categories,
        credits: draft.credits.map(({id, type, roles}) => ({id, type, roles})),
        preservation: draft.preservation,
        instructions: draft.instructions,
        compilationItemIds: draft.compilationItemIds ?? null,
    };
}

export async function fetchCompilationOptions(query: string, signal: AbortSignal): Promise<readonly (SoftwareProgram & EntityAutocompleteOption)[]> {
    const {data, error, response} = await api.GET('/api/software/lookup', {params: {query: {query}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data.items.map((program) => ({...program, type: 'software'}));
}

/** One software with its details: `GET /api/software/{id}`. */
export async function fetchSoftware(id: string, signal: AbortSignal): Promise<SoftwareDetails> {
    const {data, error, response} = await api.GET('/api/software/{id}', {params: {path: {id}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** Finds people, groups and their labels for one typed multi-select. */
export async function fetchSoftwareReferences(
    types: readonly SoftwareReferenceType[],
    query: string,
    signal: AbortSignal,
): Promise<readonly SoftwareReference[]> {
    const {data, error, response} = await api.GET('/api/entity-lookup', {params: {query: {types: types.join(','), query}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data.items;
}

/** Finds people, groups and the labels of either for the credits of software. */
export async function fetchSoftwareParticipants(query: string, signal: AbortSignal): Promise<readonly SoftwareReference[]> {
    return fetchSoftwareReferences(softwareReferenceTypeValues, query, signal);
}

/** The releases of one software, each with the hardware it shows: `GET /api/software/{id}/releases`. */
export async function fetchSoftwareReleases(id: string, signal: AbortSignal): Promise<readonly SoftwareRelease[]> {
    const {data, error, response} = await api.GET('/api/software/{id}/releases', {params: {path: {id}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** Adds software to the archive and returns its id. */
export async function createSoftware(draft: SoftwareDraft): Promise<string> {
    const {data, error, response} = await api.POST('/api/software', {body: toBody(draft)});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data.id;
}

export async function updateSoftware(id: string, draft: SoftwareDraft): Promise<void> {
    const {error, response} = await api.PUT('/api/software/{id}', {params: {path: {id}}, body: toBody(draft)});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

export async function deleteSoftware(id: string): Promise<void> {
    const {error, response} = await api.DELETE('/api/software/{id}', {params: {path: {id}}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}
