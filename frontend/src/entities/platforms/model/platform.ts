import type {Brand} from '@/shared/lib/brand';

/** The code a platform is known by: `vector` (docs/domain/hardware.md). */
export type PlatformCode = Brand<string, 'PlatformCode'>;

export interface Platform {
    readonly code: PlatformCode;
    readonly name: string;
}

const CODE = /^[a-z0-9]+(?:-[a-z0-9]+)*$/u;

export function toPlatformCode(raw: string): PlatformCode {
    if (!CODE.test(raw)) {
        throw new TypeError(`Invalid platform code: ${raw}`);
    }
    return raw as PlatformCode;
}

/** The code in a raw value from outside (the browser's storage), or null when it cannot be one. */
export function parsePlatformCode(raw: string | null): PlatformCode | null {
    return raw !== null && CODE.test(raw) ? toPlatformCode(raw) : null;
}
