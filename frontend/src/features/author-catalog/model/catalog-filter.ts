import {AUTHORS_PATH} from '@/entities/authors';
import {type Brand} from '@/shared/lib/brand';
import {isUuid} from '@/shared/lib/uuid';

/** A letter of the catalog: A–Z, А–Я (Ё counts as Е) or `#` for the rest (docs/domain/authors.md). */
export type AuthorLetter = Brand<string, 'AuthorLetter'>;
export type CountryId = Brand<string, 'CountryId'>;
export type CityId = Brand<string, 'CityId'>;

/** What the catalog shows: its filters and its page, counted from 0. */
export interface AuthorCatalogFilter {
    readonly letter: AuthorLetter | null;
    readonly country: CountryId | null;
    readonly city: CityId | null;
    readonly page: number;
}

const REST_LETTER = '#';

/** Letters following one another in Unicode, from the first one on. */
function lettersFrom(first: string, count: number): string[] {
    const start = first.charCodeAt(0);
    return Array.from({length: count}, (_, index) => String.fromCharCode(start + index));
}

/** A–Z, then А–Я: Ё lies outside the А–Я block and counts as Е. */
export const AUTHOR_LETTERS: readonly AuthorLetter[] = [...lettersFrom('A', 26), ...lettersFrom('А', 32), REST_LETTER]
    .map((letter) => letter as AuthorLetter);

/** A letter in a raw value from outside (a URL), in any case; null when it cannot be one. */
function parseLetter(raw: string | null): AuthorLetter | null {
    if (raw === null) {
        return null;
    }
    const upper = raw.toUpperCase();
    const letter = upper === 'Ё' ? 'Е' : upper;
    return AUTHOR_LETTERS.find((candidate) => candidate === letter) ?? null;
}

export function toCountryId(raw: string): CountryId {
    if (!isUuid(raw)) {
        throw new TypeError(`Invalid country id: ${raw}`);
    }
    return raw as CountryId;
}

export function toCityId(raw: string): CityId {
    if (!isUuid(raw)) {
        throw new TypeError(`Invalid city id: ${raw}`);
    }
    return raw as CityId;
}

function parseId<T>(raw: string | null, toId: (raw: string) => T): T | null {
    return raw !== null && isUuid(raw) ? toId(raw) : null;
}

/** The page in the address counts from 1. */
function parsePage(raw: string | null): number {
    return raw !== null && /^\d+$/u.test(raw) && Number(raw) >= 1 ? Number(raw) - 1 : 0;
}

/** The filter in the address of the catalog; anything that cannot be a filter is left out, a city needs its country. */
export function parseCatalogFilter(search: URLSearchParams): AuthorCatalogFilter {
    const country = parseId(search.get('country'), toCountryId);
    return {
        letter: parseLetter(search.get('letter')),
        country,
        city: country === null ? null : parseId(search.get('city'), toCityId),
        page: parsePage(search.get('page')),
    };
}

/** The address of the catalog showing the filter; what is not filtered and the first page are left out. */
export function catalogHref(filter: AuthorCatalogFilter): string {
    const search = new URLSearchParams();
    if (filter.letter !== null) {
        search.set('letter', filter.letter);
    }
    if (filter.country !== null) {
        search.set('country', filter.country);
    }
    if (filter.city !== null) {
        search.set('city', filter.city);
    }
    if (filter.page > 0) {
        search.set('page', String(filter.page + 1));
    }
    const query = search.toString();
    return query === '' ? AUTHORS_PATH : `${AUTHORS_PATH}?${query}`;
}
