import {NotFoundError} from '@/shared/api';
import {type Brand} from '@/shared/lib/brand';
import {titleProblem} from '@/shared/lib/title';
import {isUuid} from '@/shared/lib/uuid';

export type SoftwareId = Brand<string, 'SoftwareId'>;
export type SoftwareTitle = Brand<string, 'SoftwareTitle'>;
/** A calendar day, ISO `YYYY-MM-DD`, without time or time zone. */
export type ReleaseDate = Brand<string, 'ReleaseDate'>;

export interface Software {
    readonly id: SoftwareId;
    readonly title: SoftwareTitle;
    readonly releaseDate: ReleaseDate | null;
}

/** One software with everything shown about it. */
export interface SoftwareDetails extends Software {
    readonly description: string | null;
}

const ISO_DATE = /^\d{4}-\d{2}-\d{2}$/;

export function toSoftwareId(raw: string): SoftwareId {
    if (!isUuid(raw)) {
        throw new TypeError(`Invalid software id: ${raw}`);
    }
    return raw as SoftwareId;
}

/** The id in a raw value from outside (a URL), or null when it cannot be one. */
export function parseSoftwareId(raw: string | undefined): SoftwareId | null {
    return raw !== undefined && isUuid(raw) ? toSoftwareId(raw) : null;
}

/** The id of a page's software from its URL; one that cannot be an id means the page does not exist. */
export function requireSoftwareId(raw: string | undefined): SoftwareId {
    const id = parseSoftwareId(raw);
    if (id === null) {
        throw new NotFoundError('software.not-found');
    }
    return id;
}

export function toSoftwareTitle(raw: string): SoftwareTitle {
    const problem = titleProblem(raw);
    if (problem !== null) {
        throw new TypeError(`Invalid software title: ${problem}`);
    }
    return raw as SoftwareTitle;
}

export function toReleaseDate(raw: string): ReleaseDate {
    if (!ISO_DATE.test(raw) || Number.isNaN(Date.parse(`${raw}T00:00:00Z`))) {
        throw new TypeError(`Invalid release date: ${raw}`);
    }
    return raw as ReleaseDate;
}
