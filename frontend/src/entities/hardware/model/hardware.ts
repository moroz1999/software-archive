import {type components, NotFoundError} from '@/shared/api';
import type {UiLanguage} from '@/shared/lib/i18n';

/** What is kept in the management: a group of hardware or an item of it. */
export type HardwareKind = 'group' | 'hardware';

/** The names in the languages of the site; the Russian one is always there. */
export type HardwareNames = Readonly<Partial<Record<UiLanguage, string>>> & {readonly ru: string};

/** A group of the reference book in its order, named, with its hardware by name. */
export type HardwareGroupItem = components['schemas']['HardwareGroupDto'];

/** A group or an item of hardware as its form edits it; only an item has a group and platforms. */
export interface HardwareDetails {
    readonly kind: HardwareKind;
    readonly code: string;
    readonly names: HardwareNames;
    readonly group: string | null;
    readonly platforms: readonly string[];
}

const CODE = /^[a-z0-9]+(?:-[a-z0-9]+)*$/u;

/** Whether a raw value is a code of hardware or of a group of it. */
export function isHardwareCode(raw: string): boolean {
    return CODE.test(raw);
}

/** The code of the kind in a route parameter; one that cannot be a code is a page that does not exist. */
export function requireHardwareCode(kind: HardwareKind, raw: string | undefined): string {
    if (raw === undefined || !isHardwareCode(raw)) {
        throw new NotFoundError(kind === 'group' ? 'hardware.group-not-found' : 'hardware.not-found');
    }
    return raw;
}

/** The name visitors see: in the language given, else the Russian one. */
export function hardwareName(names: HardwareNames, language: UiLanguage): string {
    return names[language] ?? names.ru;
}
