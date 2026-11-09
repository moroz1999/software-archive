import {toGroupId} from '@/entities/groups';
import {getJson, mapResponse, type Page, parsePage, readNullableString, readObject, readString} from '@/shared/api';
import type {GroupCatalogEntry} from '../model/catalog';
import type {GroupCatalogFilter} from '../model/catalog-filter';

/** Entries on one page of the catalog. */
const PAGE_SIZE = 100;

function parseEntry(item: unknown): GroupCatalogEntry {
    const dto = readObject(item, 'catalog entry');
    return {
        groupId: toGroupId(readString(dto, 'groupId')),
        name: readString(dto, 'name'),
        groupName: readNullableString(dto, 'groupName'),
    };
}

/** A page of the catalog: `GET /api/groups`, with the filters that are set. */
export async function fetchGroupCatalogPage(filter: GroupCatalogFilter, signal: AbortSignal): Promise<Page<GroupCatalogEntry>> {
    const search = new URLSearchParams({page: String(filter.page), size: String(PAGE_SIZE)});
    for (const [key, value] of [['letter', filter.letter], ['country', filter.country], ['city', filter.city]] as const) {
        if (value !== null) {
            search.set(key, value);
        }
    }
    const json = await getJson(`/api/groups?${search.toString()}`, signal);
    return mapResponse(() => parsePage(json, parseEntry));
}
