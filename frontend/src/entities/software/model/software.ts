import {NotFoundError} from '@/shared/api';
import {type Brand} from '@/shared/lib/brand';
import {titleProblem} from '@/shared/lib/title';
import {isUuid} from '@/shared/lib/uuid';

export type SoftwareId = Brand<string, 'SoftwareId'>;
export type SoftwareTitle = Brand<string, 'SoftwareTitle'>;
/** A calendar day, ISO `YYYY-MM-DD`, without time or time zone. */
export type ReleaseDate = Brand<string, 'ReleaseDate'>;
/** The code of the platform software is for: `vector` (docs/domain/hardware.md). */
export type SoftwarePlatformCode = Brand<string, 'SoftwarePlatformCode'>;

export interface Software {
    readonly id: SoftwareId;
    readonly title: SoftwareTitle;
    readonly releaseDate: ReleaseDate | null;
}

/** One software with everything shown about it; its base hardware in the order of the groups, its categories in the order of the tree. */
export interface SoftwareDetails extends Software {
    readonly description: string | null;
    readonly platform: SoftwarePlatform;
    readonly hardware: readonly Hardware[];
    readonly categories: readonly SoftwareCategory[];
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

/** The groups hardware is shown under, in the order they are shown (docs/domain/hardware.md). */
export const HARDWARE_GROUPS = ['computers', 'storage', 'dos', 'sound', 'controls', 'expansion'] as const;

export type HardwareGroup = (typeof HARDWARE_GROUPS)[number];

/** An item of the hardware reference book. */
export interface Hardware {
    readonly code: string;
    readonly name: string;
    readonly group: HardwareGroup;
}

const ISO_DATE = /^\d{4}-\d{2}-\d{2}$/;
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

export function toReleaseDate(raw: string): ReleaseDate {
    if (!ISO_DATE.test(raw) || Number.isNaN(Date.parse(`${raw}T00:00:00Z`))) {
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

const HARDWARE_GROUP_SET: ReadonlySet<string> = new Set(HARDWARE_GROUPS);

export function toHardwareGroup(raw: string): HardwareGroup {
    if (!HARDWARE_GROUP_SET.has(raw)) {
        throw new TypeError(`Invalid hardware group: ${raw}`);
    }
    return raw as HardwareGroup;
}
