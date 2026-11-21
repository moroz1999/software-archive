import type {TitleProblem} from '@/shared/lib/form-rules';

/** Translation keys of the format errors of a label's name. */
export const LABEL_NAME_PROBLEM_KEYS: Readonly<Record<TitleProblem, string>> = {
    'required': 'validation.labelNameRequired',
    'too-long': 'validation.labelNameTooLong',
};
