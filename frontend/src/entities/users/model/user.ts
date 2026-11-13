import type {Brand} from '@/shared/lib/brand';
import {isUuid} from '@/shared/lib/uuid';

/** The id of an account. */
export type UserId = Brand<string, 'UserId'>;

/** A user group: what a user may do comes from their groups (docs/domain/users.md). */
export type UserGroup = 'admin' | 'moderator' | 'member';

/** What a user may do beyond reading, as the archive tells it (docs/domain/users.md). */
export type Ability = 'add-entries' | 'keep-categories';

/** The signed-in user. */
export interface CurrentUser {
    readonly id: UserId;
    readonly name: string;
    readonly email: string;
    readonly groups: readonly UserGroup[];
    readonly abilities: readonly Ability[];
}

export function toUserId(raw: string): UserId {
    if (!isUuid(raw)) {
        throw new TypeError(`Invalid user id: ${raw}`);
    }
    return raw as UserId;
}

const USER_GROUPS: readonly string[] = ['admin', 'moderator', 'member'] satisfies UserGroup[];

/** The group of a code; null for a code this interface does not know. */
export function toUserGroup(raw: string): UserGroup | null {
    return USER_GROUPS.includes(raw) ? raw as UserGroup : null;
}

const ABILITIES: readonly string[] = ['add-entries', 'keep-categories'] satisfies Ability[];

/** The ability of a code; null for a code this interface does not know. */
export function toAbility(raw: string): Ability | null {
    return ABILITIES.includes(raw) ? raw as Ability : null;
}
