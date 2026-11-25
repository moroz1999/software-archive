import {NotFoundError} from '@/shared/api';
import type {Brand} from '@/shared/lib/brand';
import {isUuid} from '@/shared/lib/uuid';

/** The id of an account. */
export type UserId = Brand<string, 'UserId'>;

/** A user group: what a user may do comes from their groups (docs/domain/users.md). */
export type UserGroup = 'admin' | 'moderator' | 'member' | 'ai-assistant';

/** What a user may do beyond reading, as the archive tells it (docs/domain/users.md). */
export type Ability = 'add-entries' | 'keep-categories' | 'use-assistant';

/** The signed-in user. */
export interface CurrentUser {
    readonly id: UserId;
    readonly name: string;
    readonly email: string;
    readonly groups: readonly UserGroup[];
    readonly abilities: readonly Ability[];
}

/** A group an administrator gives on purpose; every confirmed user is a member besides. */
export type GrantedGroup = 'admin' | 'moderator' | 'ai-assistant';

/** An account in the list of users an administrator keeps. */
export interface UserEntry {
    readonly id: UserId;
    readonly name: string;
    readonly email: string;
    /** Whether the account opened its confirmation link. */
    readonly confirmed: boolean;
    /** When the account registered, as an ISO instant. */
    readonly registeredAt: string;
    readonly groups: readonly UserGroup[];
}

export function toUserId(raw: string): UserId {
    if (!isUuid(raw)) {
        throw new TypeError(`Invalid user id: ${raw}`);
    }
    return raw as UserId;
}

/** The id in a raw value from outside (a URL), or null when it cannot be one. */
export function parseUserId(raw: string | undefined): UserId | null {
    return raw !== undefined && isUuid(raw) ? toUserId(raw) : null;
}

/** The id of a page's user from its URL; one that cannot be an id means the page does not exist. */
export function requireUserId(raw: string | undefined): UserId {
    const id = parseUserId(raw);
    if (id === null) {
        throw new NotFoundError('user.not-found');
    }
    return id;
}

const USER_GROUPS: readonly string[] = ['admin', 'moderator', 'member', 'ai-assistant'] satisfies UserGroup[];

/** The group of a code; null for a code this interface does not know. */
export function toUserGroup(raw: string): UserGroup | null {
    return USER_GROUPS.includes(raw) ? raw as UserGroup : null;
}

const ABILITIES: readonly string[] = ['add-entries', 'keep-categories', 'use-assistant'] satisfies Ability[];

/** The ability of a code; null for a code this interface does not know. */
export function toAbility(raw: string): Ability | null {
    return ABILITIES.includes(raw) ? raw as Ability : null;
}
