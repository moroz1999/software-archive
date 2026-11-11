import {NotFoundError} from '@/shared/api';
import {type Brand} from '@/shared/lib/brand';
import {isUuid} from '@/shared/lib/uuid';

/** The id of an author: the person behind the names. */
export type AuthorId = Brand<string, 'AuthorId'>;

export interface AuthorLabel {
    readonly id: string;
    readonly name: string;
    /** Chosen as the display name. */
    readonly display: boolean;
}

/** A group the author was a member of, under its main name; `as` is the label they were a member under, unless it is their main name. */
export interface AuthorGroup {
    readonly id: string;
    readonly name: string;
    readonly as: string | null;
}

/** Where an author is located: a country, a city of it, or both; the country of a city given alone is its own. */
export interface AuthorLocation {
    readonly countryId: string | null;
    readonly country: string | null;
    readonly cityId: string | null;
    readonly city: string | null;
    readonly primary: boolean;
}

/** One author with everything shown on their page. */
export interface AuthorDetails {
    readonly id: AuthorId;
    /** The main name (docs/domain/authors.md). */
    readonly name: string;
    readonly realName: string | null;
    readonly notes: string | null;
    readonly labels: readonly AuthorLabel[];
    /** The primary location first. */
    readonly locations: readonly AuthorLocation[];
    readonly groups: readonly AuthorGroup[];
}

export function toAuthorId(raw: string): AuthorId {
    if (!isUuid(raw)) {
        throw new TypeError(`Invalid author id: ${raw}`);
    }
    return raw as AuthorId;
}

/** The id in a raw value from outside (a URL), or null when it cannot be one. */
export function parseAuthorId(raw: string | undefined): AuthorId | null {
    return raw !== undefined && isUuid(raw) ? toAuthorId(raw) : null;
}

/** The id of a page's author from its URL; one that cannot be an id means the page does not exist. */
export function requireAuthorId(raw: string | undefined): AuthorId {
    const id = parseAuthorId(raw);
    if (id === null) {
        throw new NotFoundError('author.not-found');
    }
    return id;
}
