import {NotFoundError} from '@/shared/api';
import type {Brand} from '@/shared/lib/brand';
import type {UiLanguage} from '@/shared/lib/i18n';

/** The code an item of hardware is known by: lower-case Latin letters and digits, words joined by a hyphen. */
export type HardwareCode = Brand<string, 'HardwareCode'>;

/** The code a group of hardware is known by, of the same form. */
export type HardwareGroupCode = Brand<string, 'HardwareGroupCode'>;

/** What is kept in the management: a group of hardware or an item of it. */
export type HardwareKind = 'group' | 'hardware';

/** The names in the languages of the site; the Russian one is always there. */
export type HardwareNames = Readonly<Partial<Record<UiLanguage, string>>> & {readonly ru: string};

/** A group of the reference book in its order, named, with its hardware by name. */
export interface HardwareGroupItem {
    readonly code: HardwareGroupCode;
    readonly name: string;
    readonly hardware: readonly {readonly code: HardwareCode; readonly name: string}[];
}

/** A group or an item of hardware as its form edits it; only an item has a group and platforms. */
export interface HardwareDetails {
    readonly kind: HardwareKind;
    readonly code: HardwareCode | HardwareGroupCode;
    readonly names: HardwareNames;
    readonly group: HardwareGroupCode | null;
    readonly platforms: readonly string[];
}

const CODE = /^[a-z0-9]+(?:-[a-z0-9]+)*$/u;

/** Whether a raw value is a code of hardware or of a group of it. */
export function isHardwareCode(raw: string): boolean {
    return CODE.test(raw);
}

export function toHardwareCode(raw: string): HardwareCode {
    if (!isHardwareCode(raw)) {
        throw new TypeError(`Invalid hardware code: ${raw}`);
    }
    return raw as HardwareCode;
}

export function toHardwareGroupCode(raw: string): HardwareGroupCode {
    if (!isHardwareCode(raw)) {
        throw new TypeError(`Invalid hardware group code: ${raw}`);
    }
    return raw as HardwareGroupCode;
}

/** The code of the kind in a route parameter; one that cannot be a code is a page that does not exist. */
export function requireHardwareCode(kind: HardwareKind, raw: string | undefined): HardwareCode | HardwareGroupCode {
    if (raw === undefined || !isHardwareCode(raw)) {
        throw new NotFoundError(kind === 'group' ? 'hardware.group-not-found' : 'hardware.not-found');
    }
    return kind === 'group' ? toHardwareGroupCode(raw) : toHardwareCode(raw);
}

/** The name visitors see: in the language given, else the Russian one. */
export function hardwareName(names: HardwareNames, language: UiLanguage): string {
    return names[language] ?? names.ru;
}
