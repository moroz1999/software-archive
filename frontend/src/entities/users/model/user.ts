import {type components, NotFoundError} from '@/shared/api';
import {isUuid} from '@/shared/lib/uuid';

type Schemas = components['schemas'];

/** A user group: what a user may do comes from their groups (docs/domain/users.md). */
export type UserGroup = Schemas['UserGroup'];

/** What a user may do beyond reading, as the archive tells it (docs/domain/users.md). */
export type Ability = Schemas['Ability'];

/** The signed-in user. */
export type CurrentUser = Schemas['CurrentUserDto'];

/** A group an administrator gives on purpose; every confirmed user is a member besides. */
export type GrantedGroup = Extract<UserGroup, 'admin' | 'moderator' | 'ai-assistant'>;

/** An account in the list of users an administrator keeps. */
export type UserEntry = Schemas['UserEntryDto'];

/** The id in a raw value from outside (a URL), or null when it cannot be one. */
export function parseUserId(raw: string | undefined): string | null {
    return raw !== undefined && isUuid(raw) ? raw : null;
}

/** The id of a page's user from its URL; one that cannot be an id means the page does not exist. */
export function requireUserId(raw: string | undefined): string {
    const id = parseUserId(raw);
    if (id === null) {
        throw new NotFoundError('user.not-found');
    }
    return id;
}
