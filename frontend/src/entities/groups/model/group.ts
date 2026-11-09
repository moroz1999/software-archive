import {NotFoundError} from '@/shared/api';
import {type Brand} from '@/shared/lib/brand';
import {isUuid} from '@/shared/lib/uuid';

/** The id of a group: the team behind its names. */
export type GroupId = Brand<string, 'GroupId'>;

export interface GroupLabel {
    readonly id: string;
    readonly name: string;
}

/** Where a group is located: a country, a city of it, or both. */
export interface GroupLocation {
    readonly country: string | null;
    readonly city: string | null;
    readonly primary: boolean;
}

/** A member under the label they were a member under; the person behind it when known, with their main name unless it is that label. */
export interface GroupMember {
    readonly name: string;
    readonly personId: string | null;
    readonly personName: string | null;
}

/** A group inside another one, or one containing it, under its main name. */
export interface RelatedGroup {
    readonly id: GroupId;
    readonly name: string;
}

/** One group with everything shown on its page. */
export interface GroupDetails {
    readonly id: GroupId;
    /** The main name (docs/domain/authors.md). */
    readonly name: string;
    readonly groupName: string;
    readonly notes: string | null;
    readonly types: readonly string[];
    readonly labels: readonly GroupLabel[];
    /** The primary location first. */
    readonly locations: readonly GroupLocation[];
    readonly members: readonly GroupMember[];
    readonly memberGroups: readonly RelatedGroup[];
    readonly groups: readonly RelatedGroup[];
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
