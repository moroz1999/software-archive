import {type Addition, type ExternalOrigin, NotFoundError} from '@/shared/api';
import {type Brand} from '@/shared/lib/brand';
import {titleProblem} from '@/shared/lib/form-rules';
import {isUuid} from '@/shared/lib/uuid';
import type {MemberRoleValue} from '@/shared/ui/member-role-editor';
import type {Screenshot} from './screenshot';

export type SoftwareId = Brand<string, 'SoftwareId'>;
export type SoftwareTitle = Brand<string, 'SoftwareTitle'>;
/** A calendar day, ISO `YYYY-MM-DD`, without time or time zone. */
export type ReleaseDate = Brand<string, 'ReleaseDate'>;
/** The code of the platform software is for: `vector` (docs/domain/hardware.md). */
export type SoftwarePlatformCode = Brand<string, 'SoftwarePlatformCode'>;

export const SOFTWARE_REFERENCE_TYPES = ['author', 'author-label', 'group', 'group-label'] as const;
export type SoftwareReferenceType = (typeof SOFTWARE_REFERENCE_TYPES)[number];

/** A person, group or one of their labels chosen under the exact name shown in the form. */
export interface SoftwareReference {
    readonly id: string;
    readonly type: SoftwareReferenceType;
    readonly title: string;
    /** The person or group page this exact name belongs to; absent in values not loaded as software details. */
    readonly ownerId?: string | null;
}

/** A person, a group or the exact label of either credited on software, with their roles; publishing and developing are roles. */
export interface SoftwareCredit extends MemberRoleValue {
    readonly type: SoftwareReferenceType;
    /** The person or group page a label belongs to; absent in values not loaded as software details. */
    readonly ownerId?: string | null;
}

/** Whether software can be had and how (docs/domain/software.md); unknown until it is known. */
export const PRESERVATION_STATUSES = ['unknown', 'missing-in-action', 'donationware', 'in-sales'] as const;
export type PreservationStatus = (typeof PRESERVATION_STATUSES)[number];

const PRESERVATION_STATUS_SET: ReadonlySet<string> = new Set(PRESERVATION_STATUSES);

export function toPreservationStatus(raw: string): PreservationStatus {
    if (!PRESERVATION_STATUS_SET.has(raw)) {
        throw new TypeError(`Invalid preservation status: ${raw}`);
    }
    return raw as PreservationStatus;
}

/** The page of software in an archive it was imported from. */
export interface SoftwareSource {
    readonly archive: string;
    readonly url: string;
}

export interface SoftwareProgram {
    readonly id: SoftwareId;
    readonly title: SoftwareTitle;
}

export interface Software {
    readonly id: SoftwareId;
    readonly title: SoftwareTitle;
    readonly releaseDate: ReleaseDate | null;
    /** The screenshots its card shows, as lists give them (docs/domain/software.md#screenshots). */
    readonly screenshots?: readonly Screenshot[];
    /** Who made it, as its card names them (docs/domain/software.md#card). */
    readonly madeBy?: readonly string[];
    /** The rating its card shows, from a list that gives it; 0 and 0 for software nobody voted on (docs/domain/voting.md). */
    readonly rating?: CardRating;
}

/** The rating of software and the number of votes behind it. */
export interface CardRating {
    readonly rating: number;
    readonly votesAmount: number;
}

/** One software with everything shown about it; its base hardware in the order of the groups, its categories in the order of the tree. */
export interface SoftwareDetails extends Software {
    readonly description: string | null;
    /** How it is loaded, run and played, apart from its description; none when unknown. */
    readonly instructions?: string | null;
    readonly platform: SoftwarePlatform;
    readonly hardware: readonly Hardware[];
    readonly categories: readonly SoftwareCategory[];
    readonly compilationItems?: readonly SoftwareProgram[];
    readonly compilations?: readonly SoftwareProgram[];
    readonly credits?: readonly SoftwareCredit[];
    /** The pages of the archives it was imported from (docs/domain/imports.md). */
    readonly sources?: readonly SoftwareSource[];
    /** Where imports took it from, with its ids there. */
    readonly origins: readonly ExternalOrigin[];
    readonly preservation: PreservationStatus;
    /** Who added it and when. */
    readonly added: Addition;
    /** Whether whoever reads it may change it, as the archive tells. */
    readonly canChange: boolean;
}

/** A category software is in, with its branch from the top of the tree down to itself (docs/domain/categories.md). */
export interface SoftwareCategory {
    readonly id: string;
    readonly title: string;
    readonly branch: readonly CategoryStep[];
}

/** A category on the branch down to a category software is in. */
export interface CategoryStep {
    readonly id: string;
    readonly title: string;
}

/** The platform software is for, as the software shows it. */
export interface SoftwarePlatform {
    readonly code: SoftwarePlatformCode;
    readonly name: string;
}

/**
 * An item of the hardware reference book with its group, named in the interface language; the archive gives hardware
 * in the order of its groups (docs/domain/hardware.md).
 */
export interface Hardware {
    readonly code: string;
    readonly name: string;
    readonly group: string;
    readonly groupName: string;
}

const RELEASE_DATE = /^(\d{4})(?:-(\d{2})-(\d{2}))?$/;
const PLATFORM_CODE = /^[a-z0-9]+(?:-[a-z0-9]+)*$/u;

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

export function toReleaseDate(raw: string): ReleaseDate {
    if (!isReleaseDate(raw)) {
        throw new TypeError(`Invalid release date: ${raw}`);
    }
    return raw as ReleaseDate;
}

export function toSoftwarePlatformCode(raw: string): SoftwarePlatformCode {
    if (!PLATFORM_CODE.test(raw)) {
        throw new TypeError(`Invalid platform code: ${raw}`);
    }
    return raw as SoftwarePlatformCode;
}

const SOFTWARE_REFERENCE_TYPE_SET: ReadonlySet<string> = new Set(SOFTWARE_REFERENCE_TYPES);

export function toSoftwareReferenceType(raw: string): SoftwareReferenceType {
    if (!SOFTWARE_REFERENCE_TYPE_SET.has(raw)) {
        throw new TypeError(`Invalid software reference type: ${raw}`);
    }
    return raw as SoftwareReferenceType;
}
