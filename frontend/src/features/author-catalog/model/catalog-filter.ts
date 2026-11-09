import {AUTHORS_PATH} from '@/entities/authors';
import {type CityId, type CountryId, parseCityId, parseCountryId} from '@/entities/locations';
import {type LabelLetter, parseLabelLetter} from '@/entities/labels';
import {pageParam, parsePageParam} from '@/shared/lib/page-param';

/** What the catalog shows: its filters and its page, counted from 0. */
export interface AuthorCatalogFilter {
    readonly letter: LabelLetter | null;
    readonly country: CountryId | null;
    readonly city: CityId | null;
    readonly page: number;
}

/** The filter in the address of the catalog; anything that cannot be a filter is left out, a city needs its country. */
export function parseCatalogFilter(search: URLSearchParams): AuthorCatalogFilter {
    const country = parseCountryId(search.get('country'));
    return {
        letter: parseLabelLetter(search.get('letter')),
        country,
        city: country === null ? null : parseCityId(search.get('city')),
        page: parsePageParam(search.get('page')),
    };
}

/** The address of the catalog showing the filter; what is not filtered and the first page are left out. */
export function catalogHref(filter: AuthorCatalogFilter): string {
    const search = new URLSearchParams();
    for (const [key, value] of [['letter', filter.letter], ['country', filter.country], ['city', filter.city], ['page', pageParam(filter.page)]] as const) {
        if (value !== null) {
            search.set(key, value);
        }
    }
    const query = search.toString();
    return query === '' ? AUTHORS_PATH : `${AUTHORS_PATH}?${query}`;
}
