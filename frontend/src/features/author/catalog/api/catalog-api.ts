import {toAuthorId} from '@/entities/authors';
import type {PlatformCode} from '@/entities/platforms';
import {getJson, mapResponse, type Page, parsePage, readNullableString, readObject, readString} from '@/shared/api';
import type {AuthorCatalogEntry} from '../model/catalog';
import type {AuthorCatalogFilter} from '../model/catalog-filter';

/** Entries on one page of the catalog. */
const PAGE_SIZE = 100;

function parseEntry(item: unknown): AuthorCatalogEntry {
    const dto = readObject(item, 'catalog entry');
    return {
        personId: toAuthorId(readString(dto, 'personId')),
        name: readString(dto, 'name'),
        realName: readNullableString(dto, 'realName'),
        labelOf: readNullableString(dto, 'labelOf'),
    };
}

/** A page of the catalog: `GET /api/authors`, with the filters that are set, of the platform when one is chosen. */
export async function fetchAuthorCatalogPage(
    filter: AuthorCatalogFilter,
    platform: PlatformCode | null,
    signal: AbortSignal,
): Promise<Page<AuthorCatalogEntry>> {
    const search = new URLSearchParams({page: String(filter.page), size: String(PAGE_SIZE)});
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
