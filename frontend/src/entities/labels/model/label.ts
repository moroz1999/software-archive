import type {LabelId} from './label-id';

/** Whom a label belongs to once a person attaching it has said so: a person or a group. */
export interface LabelOwner {
    readonly type: 'author' | 'group';
    readonly id: string;
}

/** A group the label was a member of, under the group's main name. */
export interface LabelGroup {
    readonly id: string;
    readonly name: string;
}

/** The page of a label: its name, its owner when known, the groups it was a member of. */
export interface LabelDetails {
    readonly id: LabelId;
    readonly name: string;
    readonly owner: LabelOwner | null;
    readonly groups: readonly LabelGroup[];
    /** The reader may attach the label to its owner. */
    readonly canChange: boolean;
}

/** A person or a group a label of nobody can be attached to, as the lookup finds them. */
export interface LabelOwnerOption extends LabelOwner {
    readonly title: string;
}
