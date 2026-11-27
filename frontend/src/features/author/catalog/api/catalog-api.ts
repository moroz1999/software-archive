import {api, apiError, type components} from '@/shared/api';
import type {AuthorCatalogEntry, AuthorCatalogPage} from '../model/catalog';
import type {AuthorCatalogFilter} from '../model/catalog-filter';

/** An entry is of a person or of a label, never both. */
function toEntry({personId, labelId, name, note}: components['schemas']['AuthorCatalogEntryDto']): AuthorCatalogEntry {
    if (personId !== null && labelId === null) {
        return {kind: 'person', personId: personId, name, note};
    }
    if (labelId !== null && personId === null) {
        return {kind: 'label', labelId: labelId, name, note};
    }
    throw new TypeError('A catalog entry is of a person or of a label, never both or neither.');
}

/** A page of the catalog: `GET /api/authors`, with the filters that are set, of the platform when one is chosen. */
export async function fetchAuthorCatalogPage(
    filter: AuthorCatalogFilter,
    platform: string | null,
    signal: AbortSignal,
): Promise<AuthorCatalogPage> {
    const {data, error, response} = await api.GET('/api/authors', {
        params: {query: {page: filter.page, letter: filter.letter, country: filter.country, city: filter.city, platform}},
        signal,
    });
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return {...data, content: data.content.map(toEntry)};
}
