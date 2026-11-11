import type {AuthorFormErrors} from '../model/author-form';

/** Translation keys of format errors of the author form. */
export const REAL_NAME_PROBLEM_KEYS: Readonly<Record<NonNullable<AuthorFormErrors['realName']>, string>> = {
    'nameless': 'validation.authorNameless',
    'too-long': 'validation.realNameTooLong',
};
