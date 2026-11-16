import type {GroupId} from '@/entities/groups';
import type {LabelId} from '@/entities/labels';

/** An entry of the catalog: a group under its name, or one of its labels with the group's name next to it (docs/domain/authors.md). */
export type GroupCatalogEntry = GroupEntry | GroupLabelEntry;

/** A group under its name, leading to the group. */
export interface GroupEntry {
    readonly kind: 'group';
    readonly groupId: GroupId;
    readonly name: string;
    readonly note: string | null;
}

/** A label of a group, leading to its own page. */
export interface GroupLabelEntry {
    readonly kind: 'label';
    readonly labelId: LabelId;
    readonly name: string;
    /** The name of the group, shown next to the label. */
    readonly note: string | null;
}
