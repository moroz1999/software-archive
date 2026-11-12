import type {PlatformCode} from '@/entities/platforms';
import {parseSoftware} from '@/entities/software';
import {getJson, mapResponse, readArray, readNumber, readObject, readString, SystemError} from '@/shared/api';
import type {FoundSet, FullSearchPage} from '../model/found';
import type {FullSearchFilter} from '../model/full-search-filter';
import {parseFoundAuthor, parseFoundGroup} from './found-api';

function parseSet(value: unknown): FoundSet {
    const dto = readObject(value, 'set');
    const total = readNumber(dto, 'total');
    const items = readArray(dto['items'], 'set items');
    const type = readString(dto, 'type');
    switch (type) {
        case 'author':
            return {kind: 'author', total, items: items.map(parseFoundAuthor)};
        case 'group':
            return {kind: 'group', total, items: items.map(parseFoundGroup)};
        case 'software':
            return {kind: 'software', total, items: items.map(parseSoftware)};
        default:
            throw new SystemError('response', `Unknown kind of search results: ${type}`);
    }
}

/** A page of the full search: `GET /api/search`, of the kinds chosen and of the platform when one is chosen. */
export async function fetchFullSearch(filter: FullSearchFilter, platform: PlatformCode | null, signal: AbortSignal): Promise<FullSearchPage> {
    const search = new URLSearchParams({q: filter.keyword ?? ''});
    if (filter.types.length > 0) {
        search.set('types', filter.types.join(','));
    }
    search.set('page', String(filter.page));
    if (platform !== null) {
        search.set('platform', platform);
    }
    const json = await getJson(`/api/search?${search.toString()}`, signal);
    return mapResponse(() => {
        const dto = readObject(json, 'full search');
        const page = readObject(dto['page'], 'page metadata');
        return {
            sets: readArray(dto['sets'], 'sets').map(parseSet),
            page: {
                number: readNumber(page, 'number'),
                size: readNumber(page, 'size'),
                totalElements: readNumber(page, 'totalElements'),
                totalPages: readNumber(page, 'totalPages'),
            },
        };
    });
}
