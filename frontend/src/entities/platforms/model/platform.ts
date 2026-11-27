import type {components} from '@/shared/api';

export type Platform = components['schemas']['PlatformDto'];

const CODE = /^[a-z0-9]+(?:-[a-z0-9]+)*$/u;

/** The code in a raw value from outside (the browser's storage), or null when it cannot be one. */
export function parsePlatformCode(raw: string | null): string | null {
    return raw !== null && CODE.test(raw) ? raw : null;
}
