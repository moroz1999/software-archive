import type {components} from '@/shared/api';

/** Whom a label belongs to once a person attaching it has said so: a person or a group. */
export interface LabelOwner {
    readonly type: 'author' | 'group';
    readonly id: string;
}

type Schemas = components['schemas'];

/** The owner of a label as its page shows them: a person by the real name with the main name as the note, a group by its name. */
export type NamedLabelOwner = Schemas['LabelDetailsDto.OwnerDto'];
/** A group the label was a member of, under the group's main name. */
export type LabelGroup = Schemas['LabelDetailsDto.MemberOfDto'];
/** A place the label is located in; the country of a city given alone is its country, none while it is not known. */
export type LabelLocation = Schemas['LabelDetailsDto.LocationDto'];
/**
 * The page of a label: its name, its owner when known, the groups it was a member of, its locations and sources, and
 * what the reader may do with it.
 */
export type LabelDetails = Schemas['LabelDetailsDto'];

/** A person or a group a label of nobody can be attached to, as the lookup finds them. */
export interface LabelOwnerOption extends LabelOwner {
    readonly title: string;
}

/** A label of nobody to add: its name and its locations. */
export type NewLabelDraft = Omit<LabelDraft, 'owner'>;

/** A label to be saved, as its form produced it: whose it is after saving, or nobody's. */
export interface LabelDraft {
    readonly name: string;
    readonly owner: (LabelOwner & {readonly display: boolean}) | null;
    readonly locations: readonly {readonly country: string | null; readonly city: string | null; readonly primary: boolean}[];
}
