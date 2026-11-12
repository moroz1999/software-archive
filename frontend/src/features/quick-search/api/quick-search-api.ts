import {toAuthorId} from '@/entities/authors';
import {toGroupId} from '@/entities/groups';
import type {PlatformCode} from '@/entities/platforms';
import {parseSoftware} from '@/entities/software';
import {getJson, type JsonObject, mapResponse, readArray, readNullableString, readNumber, readObject, readString} from '@/shared/api';
import type {QuickSearchResult, QuickSection} from '../model/quick-search';

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
            authors: parseSection(dto['authors'], 'author', (item) => {
                const author = readObject(item, 'author');
                return {id: toAuthorId(readString(author, 'id')), name: readString(author, 'name'), realName: readNullableString(author, 'realName')};
            }),
            groups: parseSection(dto['groups'], 'group', (item) => {
                const group = readObject(item, 'group');
                return {id: toGroupId(readString(group, 'id')), name: readString(group, 'name')};
            }),
        };
    });
}
