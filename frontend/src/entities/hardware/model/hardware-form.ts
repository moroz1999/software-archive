import {perUiLanguage, SUPPORTED_UI_LANGUAGES, type UiLanguage} from '@/shared/lib/i18n';
import {type HardwareDetails, type HardwareKind, type HardwareNames, isHardwareCode} from './hardware';

/** The longest name of hardware or of a group of it (docs/domain.md#common-rules). */
const NAME_MAX_LENGTH = 255;

/**
 * What the form of a group or of an item of hardware holds while it is filled: the code, a name per language, empty
 * when not given; an item's group, empty while none is chosen, and its platforms by code in the order checked.
 */
export interface HardwareFormValues {
    readonly code: string;
    readonly names: Readonly<Record<UiLanguage, string>>;
    readonly group: string;
    readonly platforms: readonly string[];
}

/** A group or an item to be added or saved, as the form produced it: only the names given. */
export interface HardwareDraft {
    readonly code: string;
    readonly names: HardwareNames;
    readonly group: string;
    readonly platforms: readonly string[];
}

export interface HardwareFormErrors {
    readonly code?: 'required' | 'pattern';
    readonly name?: 'required' | 'too-long';
    readonly group?: boolean;
    readonly platforms?: boolean;
}

export type HardwareFormResult =
    | {readonly ok: true; readonly draft: HardwareDraft}
    | {readonly ok: false; readonly errors: HardwareFormErrors};

/** The languages of the names in the order the form asks for them: Russian, which everything has, first. */
export const HARDWARE_NAME_LANGUAGES: readonly UiLanguage[] = ['ru', ...SUPPORTED_UI_LANGUAGES.filter((language) => language !== 'ru')];

/**
 * Checks the format rules of the form: a code of lower-case Latin letters and digits, words joined by a hyphen; a
 * Russian name, every name at most 255 characters; an item of hardware has a group and a platform at least.
 */
export function validateHardwareForm(kind: HardwareKind, values: HardwareFormValues): HardwareFormResult {
    const names: Partial<Record<UiLanguage, string>> = {};
    for (const language of HARDWARE_NAME_LANGUAGES) {
        const name = values.names[language].trim();
        if (name !== '') {
            names[language] = name;
        }
    }
    const code = values.code.trim();
    const errors: {code?: 'required' | 'pattern'; name?: 'required' | 'too-long'; group?: boolean; platforms?: boolean} = {};
    if (code === '') {
        errors.code = 'required';
    } else if (!isHardwareCode(code)) {
        errors.code = 'pattern';
    }
    if (names.ru === undefined) {
        errors.name = 'required';
    } else if (Object.values(names).some((name) => name.length > NAME_MAX_LENGTH)) {
        errors.name = 'too-long';
    }
    if (kind === 'hardware' && values.group === '') {
        errors.group = true;
    }
    if (kind === 'hardware' && values.platforms.length === 0) {
        errors.platforms = true;
    }
    if (names.ru === undefined || Object.keys(errors).length > 0) {
        return {ok: false, errors};
    }
    return {ok: true, draft: {code, names: {...names, ru: names.ru}, group: values.group, platforms: values.platforms}};
}

/** The form values of an existing group or item, or of an empty form. */
export function toHardwareFormValues(details?: HardwareDetails): HardwareFormValues {
    const names = perUiLanguage((language) => details?.names[language] ?? '');
    return {code: details?.code ?? '', names, group: details?.group ?? '', platforms: details?.platforms ?? []};
}
