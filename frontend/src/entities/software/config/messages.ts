import type {TitleProblem} from '@/shared/lib/title';

/** Texts of format errors of the software form. */
export const TITLE_PROBLEM_MESSAGES: Readonly<Record<TitleProblem, string>> = {
    'required': 'Enter a title.',
    'too-long': 'Keep the title within 255 characters.',
};

export const RELEASE_DATE_INVALID_MESSAGE = 'Enter a full date.';

export const PLATFORM_REQUIRED_MESSAGE = 'Choose a platform.';
