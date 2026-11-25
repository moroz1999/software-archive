import {type EmailProblem, emailProblem, type GrantedGroup, type UserEntry, type UserNameProblem, userNameProblem} from '@/entities/users';

/** The groups given on purpose, in the order the form offers them. */
export const GRANTED_GROUPS: readonly GrantedGroup[] = ['admin', 'moderator', 'ai-assistant'];

export interface UserFormValues {
    readonly name: string;
    readonly email: string;
    readonly groups: ReadonlySet<GrantedGroup>;
}

export interface UserFormErrors {
    readonly name?: UserNameProblem;
    readonly email?: EmailProblem;
}

/** What is sent once the format rules pass. */
export interface UserDraft {
    readonly name: string;
    readonly email: string;
    readonly groups: readonly GrantedGroup[];
}

/** The form starts from the account as it is: its groups given on purpose are checked. */
export function toUserFormValues(user: UserEntry): UserFormValues {
    return {
        name: user.name,
        email: user.email,
        groups: new Set(GRANTED_GROUPS.filter((group) => user.groups.includes(group))),
    };
}

/** The format errors of the values; none means they are sent as the draft. */
export function validateUserForm(values: UserFormValues): {readonly ok: true; readonly draft: UserDraft} | {readonly ok: false; readonly errors: UserFormErrors} {
    const name = userNameProblem(values.name);
    const email = emailProblem(values.email);
    if (name !== null || email !== null) {
        return {ok: false, errors: {...(name === null ? {} : {name}), ...(email === null ? {} : {email})}};
    }
    return {
        ok: true,
        draft: {name: values.name, email: values.email.trim(), groups: GRANTED_GROUPS.filter((group) => values.groups.has(group))},
    };
}
