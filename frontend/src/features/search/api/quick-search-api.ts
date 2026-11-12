import type {PlatformCode} from '@/entities/platforms';
import {parseSoftware} from '@/entities/software';
import {getJson, type JsonObject, mapResponse, readArray, readNumber, readObject} from '@/shared/api';
import type {QuickSearchResult, QuickSection} from '../model/quick-search';
import {parseFoundAuthor, parseFoundGroup} from './found-api';

function parseSection<T>(value: unknown, what: string, read: (item: unknown) => T): QuickSection<T> {
    const dto: JsonObject = readObject(value, what);
    return {items: readArray(dto['items'], what).map(read), total: readNumber(dto, 'total')};
}

/** The first matches of the keyword in every section: `GET /api/search/quick`, of the platform when one is chosen. */
export async function fetchQuickSearch(keyword: string, platform: PlatformCode | null, signal: AbortSignal): Promise<QuickSearchResult> {
    const search = new URLSearchParams({q: keyword});
    if (platform !== null) {
        search.set('platform', platform);
    }
    const json = await getJson(`/api/search/quick?${search.toString()}`, signal);
    return mapResponse(() => {
        const dto = readObject(json, 'quick search');
        return {
            software: parseSection(dto['software'], 'software', parseSoftware),
            authors: parseSection(dto['authors'], 'author', parseFoundAuthor),
            groups: parseSection(dto['groups'], 'group', parseFoundGroup),
        };
    });
}
