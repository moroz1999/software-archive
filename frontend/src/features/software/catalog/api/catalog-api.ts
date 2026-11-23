import {toCityId, toCountryId} from '@/entities/locations';
import {type PlatformCode, toPlatformCode} from '@/entities/platforms';
import {parseSoftware} from '@/entities/software';
import {getJson, type JsonObject, mapResponse, parsePage, readArray, readNumber, readObject, readString} from '@/shared/api';
import type {FacetCount, SoftwareCatalogPage} from '../model/catalog';
import type {SoftwareCatalogFilter} from '../model/catalog-filter';

/** Cards on one page of the catalog. */
const PAGE_SIZE = 20;

function parseCounts<T>(value: unknown, what: string, read: (item: JsonObject) => T): readonly FacetCount<T>[] {
    return readArray(value, what).map((item) => {
        const dto = readObject(item, what);
        return {value: read(dto), count: readNumber(dto, 'count')};
    });
}

/** A page of the catalog with the values of its filters: `GET /api/software`, of the platform when one is chosen. */
export async function fetchSoftwareCatalog(
    filter: SoftwareCatalogFilter,
    platform: PlatformCode | null,
    signal: AbortSignal,
): Promise<SoftwareCatalogPage> {
    const search = new URLSearchParams({page: String(filter.page), size: String(PAGE_SIZE)});
    const year = filter.year === null ? null : String(filter.year);
    // The platforms of the filter are a choice among all of them: with one chosen in the switch they do not count.
    const platforms = platform === null && filter.platforms.length > 0 ? filter.platforms.join(',') : null;
    const hardware = filter.hardware.length > 0 ? filter.hardware.join(',') : null;
    const values = [
        ['q', filter.keyword],
        ['year', year],
        ['category', filter.category],
        ['country', filter.country],
        ['city', filter.city],
        ['platform', platform],
        ['platforms', platforms],
        ['hardware', hardware],
    ] as const;
    for (const [key, value] of values) {
        if (value !== null) {
            search.set(key, value);
        }
    }
    const json = await getJson(`/api/software?${search.toString()}`, signal);
    return mapResponse(() => {
        const facets = readObject(readObject(json, 'catalog')['facets'], 'facets');
        return {
            page: parsePage(json, parseSoftware),
            facets: {
                years: parseCounts(facets['years'], 'year', (dto) => readNumber(dto, 'value')),
                countries: parseCounts(facets['countries'], 'country', (dto) => toCountryId(readString(dto, 'value'))),
                cities: parseCounts(facets['cities'], 'city', (dto) => toCityId(readString(dto, 'value'))),
                platforms: parseCounts(facets['platforms'], 'platform', (dto) => toPlatformCode(readString(dto, 'value'))),
                hardware: readArray(facets['hardware'], 'hardware').map((item) => {
                    const dto = readObject(item, 'hardware');
                    return {
                        value: readString(dto, 'value'),
                        name: readString(dto, 'name'),
                        group: readString(dto, 'group'),
                        groupName: readString(dto, 'groupName'),
                        count: readNumber(dto, 'count'),
                    };
                }),
            },
        };
    });
}
