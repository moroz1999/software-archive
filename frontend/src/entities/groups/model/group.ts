import {type components, NotFoundError} from '@/shared/api';
import {isUuid} from '@/shared/lib/uuid';

type Schemas = components['schemas'];

/** One group with everything shown on its page; its primary location first. */
export type GroupDetails = Schemas['GroupDetailsDto'];
/** A label of a group: one of its alternative names. */
export type GroupLabel = Schemas['GroupDetailsDto.LabelDto'];
/** Where a group is located: a country, a city of it, or both; the country of a city given alone is its own. */
export type GroupLocation = Schemas['GroupDetailsDto.LocationDto'];
/** A member under the label they were a member under; the person behind it when known, with their main name unless it is that label. */
export type GroupMember = Schemas['GroupDetailsDto.MemberDto'];
/** A group inside another one, or one containing it, under its main name. */
export type RelatedGroup = Schemas['GroupDetailsDto.RelatedGroupDto'];
export type GroupMemberRole = Schemas['GroupMemberRole'];
/** A person or one of their labels a group member can be. */
export type PersonReferenceType = Schemas['PersonReferenceType'];

/** The id in a raw value from outside (a URL), or null when it cannot be one. */
export function parseGroupId(raw: string | undefined): string | null {
    return raw !== undefined && isUuid(raw) ? raw : null;
}

/** The id of a page's group from its URL; one that cannot be an id means the page does not exist. */
export function requireGroupId(raw: string | undefined): string {
    const id = parseGroupId(raw);
    if (id === null) {
        throw new NotFoundError('group.not-found');
    }
    return id;
}
