/** The format rules of account fields, the same the archive checks (docs/domain/users.md). */

export const USER_NAME_MAX_LENGTH = 64;
export const EMAIL_MAX_LENGTH = 254;
export const PASSWORD_MIN_LENGTH = 8;
export const PASSWORD_MAX_LENGTH = 128;

export type UserNameProblem = 'required' | 'too-long';
export type EmailProblem = 'required' | 'invalid' | 'too-long';
export type PasswordProblem = 'too-short' | 'too-long';

/** A user name is not blank and at most 64 characters long. */
export function userNameProblem(value: string): UserNameProblem | null {
    if (value.trim() === '') {
        return 'required';
    }
    return value.length > USER_NAME_MAX_LENGTH ? 'too-long' : null;
}

/** An email has an address and a domain around one `@`, no spaces, at most 254 characters; spaces around it do not count. */
export function emailProblem(value: string): EmailProblem | null {
    const email = value.trim();
    if (email === '') {
        return 'required';
    }
    if (email.length > EMAIL_MAX_LENGTH) {
        return 'too-long';
    }
    return EMAIL.test(email) ? null : 'invalid';
}

const EMAIL = /^[^@\s]+@[^@\s]+$/;

/** A password has 8 to 128 characters. */
export function passwordProblem(value: string): PasswordProblem | null {
    if (value.length < PASSWORD_MIN_LENGTH) {
        return 'too-short';
    }
    return value.length > PASSWORD_MAX_LENGTH ? 'too-long' : null;
}
