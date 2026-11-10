import {NotFoundError} from '@/shared/api';
import type {Brand} from '@/shared/lib/brand';
import {isUuid} from '@/shared/lib/uuid';
import type {Hardware, ReleaseDate} from './software';

export type ReleaseId = Brand<string, 'ReleaseId'>;

/** The fixed list of release types (docs/domain/software.md). */
export const RELEASE_TYPES = [
    'original',
    'rerelease',
    'adaptation',
    'localization',
    'bugfix',
    'mod',
    'crack',
    'mia',
    'corrupted',
    'compilation',
    'incomplete',
    'demoversion',
    'unknown',
] as const;

export type ReleaseType = (typeof RELEASE_TYPES)[number];

/** A release of software, under its own title or the title of its software, with the hardware it shows. */
export interface SoftwareRelease {
    readonly id: ReleaseId;
    readonly title: string;
    readonly version: string | null;
    readonly type: ReleaseType;
    readonly releaseDate: ReleaseDate | null;
    readonly description: string | null;
    readonly hardware: readonly Hardware[];
}

/** A release as it states itself, to edit it: its own title, if any, and only its own hardware. */
export interface ReleaseDetails {
    readonly id: ReleaseId;
    readonly title: string | null;
    readonly version: string | null;
    readonly type: ReleaseType;
    readonly releaseDate: ReleaseDate | null;
    readonly description: string | null;
    readonly hardware: readonly Hardware[];
}

export function toReleaseId(raw: string): ReleaseId {
    if (!isUuid(raw)) {
        throw new TypeError(`Invalid release id: ${raw}`);
    }
    return raw as ReleaseId;
}

const RELEASE_TYPE_SET: ReadonlySet<string> = new Set(RELEASE_TYPES);

export function toReleaseType(raw: string): ReleaseType {
    if (!RELEASE_TYPE_SET.has(raw)) {
        throw new TypeError(`Invalid release type: ${raw}`);
    }
    return raw as ReleaseType;
}

/** The id of a page's release from its URL; one that cannot be an id means the page does not exist. */
export function requireReleaseId(raw: string | undefined): ReleaseId {
    if (raw === undefined || !isUuid(raw)) {
        throw new NotFoundError('release.not-found');
    }
    return toReleaseId(raw);
}
