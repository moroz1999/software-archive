import {type components, NotFoundError} from '@/shared/api';
import {isUuid} from '@/shared/lib/uuid';

type Schemas = components['schemas'];

/** A label of an author; one of them may be chosen as the display name. */
export type AuthorLabel = Schemas['AuthorDetailsDto.LabelDto'];
/** A group the author was a member of, under its main name; `as` is the label they were a member under, unless it is their main name. */
export type AuthorGroup = Schemas['AuthorDetailsDto.GroupDto'];
/** Where an author is located: a country, a city of it, or both; the country of a city given alone is its own. */
export type AuthorLocation = Schemas['AuthorDetailsDto.LocationDto'];
/** One author with everything shown on their page; the primary location first. */
export type AuthorDetails = Schemas['AuthorDetailsDto'];

/** The id in a raw value from outside (a URL), or null when it cannot be one. */
export function parseAuthorId(raw: string | undefined): string | null {
    return raw !== undefined && isUuid(raw) ? raw : null;
}

/** The id of a page's author from its URL; one that cannot be an id means the page does not exist. */
export function requireAuthorId(raw: string | undefined): string {
    const id = parseAuthorId(raw);
    if (id === null) {
        throw new NotFoundError('author.not-found');
    }
    return id;
}
