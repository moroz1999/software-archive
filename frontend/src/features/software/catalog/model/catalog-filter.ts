import {type CategoryId, requireCategoryId} from '@/entities/categories';
import {type CountryId, parseCountryId} from '@/entities/locations';
import {SOFTWARE_LIST_PATH} from '@/entities/software';
import {pageParam, parsePageParam} from '@/shared/lib/page-param';

/** What the catalog shows: its keyword, its filters and its page, counted from 0. */
export interface SoftwareCatalogFilter {
    readonly keyword: string | null;
    readonly year: number | null;
    readonly category: CategoryId | null;
    readonly country: CountryId | null;
    readonly page: number;
}

function parseKeyword(raw: string | null): string | null {
    const keyword = raw?.trim() ?? '';
    return keyword === '' ? null : keyword;
}

function parseYear(raw: string | null): number | null {
    return raw !== null && /^\d{1,4}$/u.test(raw) ? Number(raw) : null;
}

/**
 * The filter in the address of the catalog: the category from the path, the rest from the query. Anything in the query
 * that cannot be a filter is left out; a category in the path that cannot be an id means the page does not exist.
 */
export function parseCatalogFilter(category: string | undefined, search: URLSearchParams): SoftwareCatalogFilter {
    return {
        keyword: parseKeyword(search.get('q')),
        year: parseYear(search.get('year')),
        category: category === undefined ? null : requireCategoryId(category),
        country: parseCountryId(search.get('country')),
        page: parsePageParam(search.get('page')),
    };
}

/** The address of the catalog showing the filter: the category in the path; what is not set and the first page left out. */
export function catalogHref(filter: SoftwareCatalogFilter): string {
    const search = new URLSearchParams();
    const year = filter.year === null ? null : String(filter.year);
    for (const [key, value] of [['q', filter.keyword], ['year', year], ['country', filter.country], ['page', pageParam(filter.page)]] as const) {
        if (value !== null) {
            search.set(key, value);
        }
    }
    const path = filter.category === null ? SOFTWARE_LIST_PATH : `${SOFTWARE_LIST_PATH}/category/${filter.category}`;
    const query = search.toString();
    return query === '' ? path : `${path}?${query}`;
}
