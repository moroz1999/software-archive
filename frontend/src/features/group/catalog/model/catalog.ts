import type {GroupId} from '@/entities/groups';

/** An entry of the catalog: a group under its main name, or one of its other labels. */
export interface GroupCatalogEntry {
    readonly groupId: GroupId;
    readonly name: string;
    /** The name of the group, shown next to the entry; null when it is the entry's name. */
    readonly groupName: string | null;
}
