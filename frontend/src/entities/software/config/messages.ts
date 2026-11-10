import type {TitleProblem} from '@/shared/lib/title';

/** Translation keys of format errors of the software form. */
export const TITLE_PROBLEM_KEYS: Readonly<Record<TitleProblem, string>> = {
    'required': 'validation.titleRequired',
    'too-long': 'validation.titleTooLong',
};

export const RELEASE_DATE_INVALID_KEY = 'validation.releaseDateInvalid';

export const PLATFORM_REQUIRED_KEY = 'validation.platformRequired';

export const RELEASE_VERSION_TOO_LONG_KEY = 'validation.versionTooLong';

export const RELEASE_TYPE_REQUIRED_KEY = 'validation.typeRequired';
