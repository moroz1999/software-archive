import {type Addition, type ExternalOrigin, NotFoundError} from '@/shared/api';
import {type Brand} from '@/shared/lib/brand';
import {isUuid} from '@/shared/lib/uuid';

/** The id of a group: the team behind its names. */
export type GroupId = Brand<string, 'GroupId'>;

/** A label of a group: one of its alternative names. */
export interface GroupLabel {
    readonly id: string;
    readonly name: string;
}

/** Where a group is located: a country, a city of it, or both; the country of a city given alone is its own. */
export interface GroupLocation {
    readonly countryId: string | null;
    readonly country: string | null;
    readonly cityId: string | null;
    readonly city: string | null;
    readonly primary: boolean;
}

/** A member under the label they were a member under; the person behind it when known, with their main name unless it is that label. */
export interface GroupMember {
    readonly id: string | null;
    readonly type: 'author' | 'author-label';
    readonly title: string;
    readonly name: string;
    readonly personId: string | null;
    readonly personName: string | null;
    readonly roles: readonly string[];
}

/** A group inside another one, or one containing it, under its main name. */
export interface RelatedGroup {
    readonly id: GroupId;
    readonly name: string;
}

/** One group with everything shown on its page. */
export interface GroupDetails {
    readonly id: GroupId;
    /** The group's own name, the one it is shown under (docs/domain/authors.md). */
    readonly name: string;
    readonly notes: string | null;
    readonly types: readonly string[];
    readonly labels: readonly GroupLabel[];
    /** The primary location first. */
    readonly locations: readonly GroupLocation[];
    readonly members: readonly GroupMember[];
    readonly memberGroups: readonly RelatedGroup[];
    readonly groups: readonly RelatedGroup[];
    /** Where imports took it from. */
    readonly origins: readonly ExternalOrigin[];
    /** Who added it and when. */
    readonly added: Addition;
    /** Whether whoever reads it may change it, as the archive tells. */
    readonly canChange: boolean;
}

export function toGroupId(raw: string): GroupId {
    if (!isUuid(raw)) {
        throw new TypeError(`Invalid group id: ${raw}`);
    }
    return raw as GroupId;
}

/** The id in a raw value from outside (a URL), or null when it cannot be one. */
export function parseGroupId(raw: string | undefined): GroupId | null {
    return raw !== undefined && isUuid(raw) ? toGroupId(raw) : null;
}

/** The id of a page's group from its URL; one that cannot be an id means the page does not exist. */
export function requireGroupId(raw: string | undefined): GroupId {
    const id = parseGroupId(raw);
    if (id === null) {
        throw new NotFoundError('group.not-found');
    }
    return id;
}
