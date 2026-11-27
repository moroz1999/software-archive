import {api, apiError, type components} from '@/shared/api';
import type {GroupCatalogEntry, GroupCatalogPage} from '../model/catalog';
import type {GroupCatalogFilter} from '../model/catalog-filter';


/** An entry is of a group or of a label, never both. */
function toEntry({groupId, labelId, name, note}: components['schemas']['GroupCatalogEntryDto']): GroupCatalogEntry {
    if (groupId !== null && labelId === null) {
        return {kind: 'group', groupId: groupId, name, note};
    }
    if (labelId !== null && groupId === null) {
        return {kind: 'label', labelId: labelId, name, note};
    }
    throw new TypeError('A catalog entry is of a group or of a label, never both or neither.');
}

/** A page of the catalog: `GET /api/groups`, with the filters that are set, of the platform when one is chosen. */
export async function fetchGroupCatalogPage(
    filter: GroupCatalogFilter,
    platform: string | null,
    signal: AbortSignal,
): Promise<GroupCatalogPage> {
    const {data, error, response} = await api.GET('/api/groups', {
        params: {query: {page: filter.page, letter: filter.letter, country: filter.country, city: filter.city, platform}},
        signal,
    });
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return {...data, content: data.content.map(toEntry)};
}
