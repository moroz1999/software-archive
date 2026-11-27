import {pageParam, parsePageParam} from '@/shared/lib/page-param';
import {SEARCH_PATH} from '../config/paths';
import {foundKindValues} from '@/shared/api';
import type {FoundKind} from './found';

/** What the full search shows: its keyword, the kinds chosen (none for every kind) and the page, counted from 0. */
export interface FullSearchFilter {
    readonly keyword: string | null;
    readonly types: readonly FoundKind[];
    readonly page: number;
}

/** The trimmed keyword; none for a blank one. */
export function keywordOf(text: string | null): string | null {
    const keyword = text?.trim() ?? '';
    return keyword === '' ? null : keyword;
}

/** The kinds chosen, in their order; none when every kind or none of them is. */
export function chosenKinds(kinds: Iterable<string>): readonly FoundKind[] {
    const chosen = new Set(kinds);
    const known = foundKindValues.filter((kind) => chosen.has(kind));
    return known.length === foundKindValues.length ? [] : known;
}

/** The filter in the address of the full search; anything that cannot be one is left out. */
export function parseFullSearchFilter(search: URLSearchParams): FullSearchFilter {
    return {
        keyword: keywordOf(search.get('q')),
        types: chosenKinds((search.get('types') ?? '').split(',')),
        page: parsePageParam(search.get('page')),
    };
}

/** The address of the full search showing the filter; no keyword, every kind and the first page left out. */
export function fullSearchHref(filter: FullSearchFilter): string {
    const search = new URLSearchParams();
    const types = filter.types.length === 0 ? null : filter.types.join(',');
    for (const [key, value] of [['q', filter.keyword], ['types', types], ['page', pageParam(filter.page)]] as const) {
        if (value !== null) {
            search.set(key, value);
        }
    }
    const query = search.toString();
    return query === '' ? SEARCH_PATH : `${SEARCH_PATH}?${query}`;
}
