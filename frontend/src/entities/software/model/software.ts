import {type components, NotFoundError} from '@/shared/api';
import {isUuid} from '@/shared/lib/uuid';

type Schemas = components['schemas'];

/** A card of software, as every list of the archive gives it. */
export type Software = Schemas['SoftwareListItemDto'];
/** One software with everything shown about it; its base hardware in the order of the groups, its categories in the order of the tree. */
export type SoftwareDetails = Schemas['SoftwareDetailsDto'];
/** A person, a group or the exact label of either credited on software, with their roles; publishing and developing are roles. */
export type SoftwareCredit = Schemas['SoftwareDetailsDto.SoftwareCreditDto'];
/** A person, group or one of their labels offered under the exact name shown in the form. */
export type SoftwareReference = Schemas['SoftwareReferenceDto'];
export type SoftwareReferenceType = Schemas['SoftwareReferenceType'];
/** Whether software can be had and how (docs/domain/software.md); unknown until it is known. */
export type PreservationStatus = Schemas['PreservationStatus'];
export type SoftwareProgram = Schemas['SoftwareLinkDto'];
export type SoftwareCategory = Schemas['SoftwareCategoryDto'];
export type SoftwarePlatform = Schemas['SoftwarePlatformDto'];
/** An item of the hardware reference book with its group, named in the interface language (docs/domain/hardware.md). */
export type Hardware = Schemas['SoftwareHardwareDto'];

const RELEASE_DATE = /^(\d{4})(?:-(\d{2})-(\d{2}))?$/;

/** The id in a raw value from outside (a URL), or null when it cannot be one. */
export function parseSoftwareId(raw: string | undefined): string | null {
    return raw !== undefined && isUuid(raw) ? raw : null;
}

/** The id of a page's software from its URL; one that cannot be an id means the page does not exist. */
export function requireSoftwareId(raw: string | undefined): string {
    const id = parseSoftwareId(raw);
    if (id === null) {
        throw new NotFoundError('software.not-found');
    }
    return id;
}

/** Whether it is a release date: a year, `1987`, or a day of the calendar, `1987-05-12` (docs/domain/software.md). */
export function isReleaseDate(raw: string): boolean {
    const parts = RELEASE_DATE.exec(raw);
    if (parts === null) {
        return false;
    }
    const [, year, month, day] = parts;
    if (month === undefined || day === undefined) {
        return true;
    }
    const date = new Date(Date.UTC(Number(year), Number(month) - 1, Number(day)));
    return date.getUTCMonth() === Number(month) - 1 && date.getUTCDate() === Number(day);
}
