import type {EmailProblem, PasswordProblem, UserNameProblem} from '../model/account-rules';

/** The texts of the format problems of account fields. */
export const USER_NAME_PROBLEM_KEYS = {
    required: 'validation.userNameRequired',
    'too-long': 'validation.userNameTooLong',
} as const satisfies Record<UserNameProblem, string>;

export const EMAIL_PROBLEM_KEYS = {
    required: 'validation.emailRequired',
    invalid: 'validation.emailInvalid',
    'too-long': 'validation.emailTooLong',
} as const satisfies Record<EmailProblem, string>;

export const PASSWORD_PROBLEM_KEYS = {
    'too-short': 'validation.passwordTooShort',
    'too-long': 'validation.passwordTooLong',
} as const satisfies Record<PasswordProblem, string>;
