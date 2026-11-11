import type {TitleProblem} from '@/shared/lib/title';

/** Translation keys of the format errors of a category's title. */
export const TITLE_PROBLEM_KEYS: Readonly<Record<TitleProblem, string>> = {
    'required': 'validation.titleRequired',
    'too-long': 'validation.titleTooLong',
};
