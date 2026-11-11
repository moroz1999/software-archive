import type {TitleProblem} from '@/shared/lib/title';

/** Translation keys of the format errors of a group's name. */
export const NAME_PROBLEM_KEYS: Readonly<Record<TitleProblem, string>> = {
    'required': 'validation.titleRequired',
    'too-long': 'validation.titleTooLong',
};

export const TYPES_REQUIRED_KEY = 'validation.groupTypesRequired';
