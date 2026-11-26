import {toGroupId} from '@/entities/groups';
import {toLabelId} from '@/entities/labels';
import type {PlatformCode} from '@/entities/platforms';
import {getJson, mapResponse, type Page, parsePage, readNullableString, readObject, readString} from '@/shared/api';
import type {GroupCatalogEntry} from '../model/catalog';
import type {GroupCatalogFilter} from '../model/catalog-filter';


/** An entry is of a group or of a label, never both. */
function parseEntry(item: unknown): GroupCatalogEntry {
    const dto = readObject(item, 'catalog entry');
    const groupId = readNullableString(dto, 'groupId');
    const labelId = readNullableString(dto, 'labelId');
    const name = readString(dto, 'name');
    const note = readNullableString(dto, 'note');
    if ((groupId === null) === (labelId === null)) {
        throw new TypeError('A catalog entry is of a group or of a label, never both or neither.');
    }
    return groupId === null
        ? {kind: 'label', labelId: toLabelId(labelId ?? ''), name, note}
        : {kind: 'group', groupId: toGroupId(groupId), name, note};
}

/** A page of the catalog: `GET /api/groups`, with the filters that are set, of the platform when one is chosen. */
export async function fetchGroupCatalogPage(
    filter: GroupCatalogFilter,
    platform: PlatformCode | null,
    signal: AbortSignal,
): Promise<Page<GroupCatalogEntry>> {
    const search = new URLSearchParams({page: String(filter.page)});
    for (const [key, value] of [['letter', filter.letter], ['country', filter.country], ['city', filter.city]] as const) {
        if (value !== null) {
            search.set(key, value);
        }
    }
    if (platform !== null) {
        search.set('platform', platform);
    }
    const json = await getJson(`/api/groups?${search.toString()}`, signal);
    return mapResponse(() => parsePage(json, parseEntry));
}
