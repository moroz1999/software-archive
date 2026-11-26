import {toAuthorId} from '@/entities/authors';
import {toLabelId} from '@/entities/labels';
import type {PlatformCode} from '@/entities/platforms';
import {getJson, mapResponse, type Page, parsePage, readNullableString, readObject, readString} from '@/shared/api';
import type {AuthorCatalogEntry} from '../model/catalog';
import type {AuthorCatalogFilter} from '../model/catalog-filter';


/** An entry is of a person or of a label, never both. */
function parseEntry(item: unknown): AuthorCatalogEntry {
    const dto = readObject(item, 'catalog entry');
    const personId = readNullableString(dto, 'personId');
    const labelId = readNullableString(dto, 'labelId');
    const name = readString(dto, 'name');
    const note = readNullableString(dto, 'note');
    if ((personId === null) === (labelId === null)) {
        throw new TypeError('A catalog entry is of a person or of a label, never both or neither.');
    }
    return personId === null
        ? {kind: 'label', labelId: toLabelId(labelId ?? ''), name, note}
        : {kind: 'person', personId: toAuthorId(personId), name, note};
}

/** A page of the catalog: `GET /api/authors`, with the filters that are set, of the platform when one is chosen. */
export async function fetchAuthorCatalogPage(
    filter: AuthorCatalogFilter,
    platform: PlatformCode | null,
    signal: AbortSignal,
): Promise<Page<AuthorCatalogEntry>> {
    const search = new URLSearchParams({page: String(filter.page)});
    for (const [key, value] of [['letter', filter.letter], ['country', filter.country], ['city', filter.city]] as const) {
        if (value !== null) {
            search.set(key, value);
        }
    }
    if (platform !== null) {
        search.set('platform', platform);
    }
    const json = await getJson(`/api/authors?${search.toString()}`, signal);
    return mapResponse(() => parsePage(json, parseEntry));
}
