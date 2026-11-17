import type {LabelId} from './label-id';

/** Whom a label belongs to once a person attaching it has said so: a person or a group. */
export interface LabelOwner {
    readonly type: 'author' | 'group';
    readonly id: string;
}

/** The owner of a label as its page shows them: a person by the real name with the main name as the note, a group by its name. */
export interface NamedLabelOwner extends LabelOwner {
    readonly name: string;
    readonly note: string | null;
}

/** A group the label was a member of, under the group's main name. */
export interface LabelGroup {
    readonly id: string;
    readonly name: string;
}

/** A place the label is located in; the country of a city given alone is its country. */
export interface LabelLocation {
    readonly countryId: string;
    readonly country: string;
    readonly cityId: string | null;
    readonly city: string | null;
    readonly primary: boolean;
}

/** An import the label came from: the source archive, the id there and its page there, when it has one. */
export interface LabelOrigin {
    readonly origin: string;
    readonly externalId: string;
    readonly url: string | null;
}

/** The page of a label: its name, its owner when known, the groups it was a member of, its locations and sources. */
export interface LabelDetails {
    readonly id: LabelId;
    readonly name: string;
    readonly owner: NamedLabelOwner | null;
    readonly groups: readonly LabelGroup[];
    readonly locations: readonly LabelLocation[];
    readonly origins: readonly LabelOrigin[];
    /** The reader may attach the label to its owner. */
    readonly canChange: boolean;
    /** The reader may make a new person or group of the label, taking it away from whoever has it. */
    readonly canMakeOwner: boolean;
    /** The reader may edit and delete the label. */
    readonly canEdit: boolean;
    /** The reader may say whose the label is in its form. */
    readonly canChangeOwner: boolean;
}

/** A person or a group a label of nobody can be attached to, as the lookup finds them. */
export interface LabelOwnerOption extends LabelOwner {
    readonly title: string;
}

/** A label to be saved, as its form produced it: whose it is after saving, or nobody's. */
export interface LabelDraft {
    readonly name: string;
    readonly owner: (LabelOwner & {readonly display: boolean}) | null;
    readonly locations: readonly {readonly country: string; readonly city: string | null; readonly primary: boolean}[];
}
