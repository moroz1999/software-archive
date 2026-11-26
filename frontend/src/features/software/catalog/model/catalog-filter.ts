import {type CategoryId, requireCategoryId} from '@/entities/categories';
import {type CityId, type CountryId, parseCityId, parseCountryId} from '@/entities/locations';
import {parsePlatformCode, type PlatformCode} from '@/entities/platforms';
import {SOFTWARE_LIST_PATH} from '@/entities/software';
import {pageParam, parsePageParam} from '@/shared/lib/page-param';

/** The order of the cards: by title, or the best rated first (docs/domain/search.md#software-catalog). */
export type CatalogOrder = 'title' | 'rating';

/**
 * What the catalog shows: its keyword, its filters, the order of its cards and its page, counted from 0. A city is chosen within its country;
 * of the platforms any one will do, of the hardware any item of a group, and every group chosen.
 */
export interface SoftwareCatalogFilter {
    readonly keyword: string | null;
    readonly year: number | null;
    readonly category: CategoryId | null;
    readonly country: CountryId | null;
    readonly city: CityId | null;
    readonly platforms: readonly PlatformCode[];
    /** The codes of the hardware chosen. */
    readonly hardware: readonly string[];
    readonly order: CatalogOrder;
    readonly page: number;
}

const HARDWARE_CODE = /^[a-z0-9]+(-[a-z0-9]+)*$/u;

function parseKeyword(raw: string | null): string | null {
    const keyword = raw?.trim() ?? '';
    return keyword === '' ? null : keyword;
}

function parseYear(raw: string | null): number | null {
    return raw !== null && /^\d{1,4}$/u.test(raw) ? Number(raw) : null;
}

/** The values of a list joined by commas, each read by `parse`; one that cannot be a value is left out. */
function parseList<T>(raw: string | null, parse: (value: string) => T | null): readonly T[] {
    return (raw ?? '').split(',').flatMap((value) => {
        const parsed = parse(value);
        return parsed === null ? [] : [parsed];
    });
}

function parseHardwareCode(raw: string): string | null {
    return HARDWARE_CODE.test(raw) ? raw : null;
}

/**
 * The filter in the address of the catalog: the category from the path, the rest from the query. Anything in the query
 * that cannot be a filter is left out, and so is a city without its country; a category in the path that cannot be an
 * id means the page does not exist.
 */
export function parseCatalogFilter(category: string | undefined, search: URLSearchParams): SoftwareCatalogFilter {
    const country = parseCountryId(search.get('country'));
    return {
        keyword: parseKeyword(search.get('q')),
        year: parseYear(search.get('year')),
        category: category === undefined ? null : requireCategoryId(category),
        country,
        city: country === null ? null : parseCityId(search.get('city')),
        platforms: parseList(search.get('platforms'), parsePlatformCode),
        hardware: parseList(search.get('hardware'), parseHardwareCode),
        order: search.get('order') === 'rating' ? 'rating' : 'title',
        page: parsePageParam(search.get('page')),
    };
}

function joined(values: readonly string[]): string | null {
    return values.length === 0 ? null : values.join(',');
}

/** The address of the catalog showing the filter: the category in the path; what is not set and the first page left out. */
export function catalogHref(filter: SoftwareCatalogFilter): string {
    const search = new URLSearchParams();
    const year = filter.year === null ? null : String(filter.year);
    const values = [
        ['q', filter.keyword],
        ['year', year],
        ['country', filter.country],
        ['city', filter.city],
        ['platforms', joined(filter.platforms)],
        ['hardware', joined(filter.hardware)],
        ['order', filter.order === 'rating' ? 'rating' : null],
        ['page', pageParam(filter.page)],
    ] as const;
    for (const [key, value] of values) {
        if (value !== null) {
            search.set(key, value);
        }
    }
    const path = filter.category === null ? SOFTWARE_LIST_PATH : `${SOFTWARE_LIST_PATH}/category/${filter.category}`;
    const query = search.toString();
    return query === '' ? path : `${path}?${query}`;
}

/** The address of the whole catalog narrowed only by the values given: where a fact of software leads. */
export function catalogHrefOf(only: Partial<Pick<SoftwareCatalogFilter, 'year' | 'category'>>): string {
    return catalogHref({keyword: null, year: null, category: null, country: null, city: null, platforms: [], hardware: [], order: 'title', page: 0, ...only});
}
