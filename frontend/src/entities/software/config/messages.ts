import type {TFunction} from 'i18next';
import type {BusinessMessages} from '@/shared/api';
import type {TitleProblem} from '@/shared/lib/form-rules';

export function compilationMessages(t: TFunction): BusinessMessages {
    return {
        'software.compilation-item-unknown': t('errors.compilationItemUnknown'),
        'software.compilation-cycle': t('errors.compilationCycle'),
    };
}

/** Translation keys of format errors of the software form. */
export const TITLE_PROBLEM_KEYS: Readonly<Record<TitleProblem, string>> = {
    'required': 'validation.titleRequired',
    'too-long': 'validation.titleTooLong',
};

export const RELEASE_DATE_INVALID_KEY = 'validation.releaseDateInvalid';

export const RELEASE_DATE_HINT_KEY = 'fields.releaseDateHint';

export const PLATFORM_REQUIRED_KEY = 'validation.platformRequired';

export const RELEASE_VERSION_TOO_LONG_KEY = 'validation.versionTooLong';

export const RELEASE_TYPE_REQUIRED_KEY = 'validation.typeRequired';

export const LINEAGE_SOURCE_TOO_LONG_KEY = 'validation.lineageSourceTooLong';
