import {api, apiError, type components} from '@/shared/api';
import {type AuthorDetails} from '../model/author';
import type {AuthorDraft} from '../model/author-form';

function toBody(draft: AuthorDraft): components['schemas']['AuthorRequestDto'] {
    return {
        realName: draft.realName,
        notes: draft.notes,
        labels: draft.labels.map((label) => ({id: label.id, name: label.name, display: label.display})),
        locations: draft.locations.map((location) => ({country: location.country, city: location.city, primary: location.primary})),
    };
}

/** One author with their details: `GET /api/authors/{id}`. */
export async function fetchAuthor(id: string, signal: AbortSignal): Promise<AuthorDetails> {
    const {data, error, response} = await api.GET('/api/authors/{id}', {params: {path: {id}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** Adds an author to the archive and returns their id. */
export async function createAuthor(draft: AuthorDraft): Promise<string> {
    const {data, error, response} = await api.POST('/api/authors', {body: toBody(draft)});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data.id;
}

export async function updateAuthor(id: string, draft: AuthorDraft): Promise<void> {
    const {error, response} = await api.PUT('/api/authors/{id}', {params: {path: {id}}, body: toBody(draft)});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

/** Deletes the person; their labels stay in the archive as labels of nobody. */
export async function deleteAuthor(id: string): Promise<void> {
    const {error, response} = await api.DELETE('/api/authors/{id}', {params: {path: {id}}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}
