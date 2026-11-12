import type {AuthorId} from '@/entities/authors';
import type {GroupId} from '@/entities/groups';
import type {Software} from '@/entities/software';

/** The first matches of a section and the count of all of them. */
export interface QuickSection<T> {
    readonly items: readonly T[];
    readonly total: number;
}

/** A person under the main name; the real name when known and not the main name. */
export interface QuickAuthor {
    readonly id: AuthorId;
    readonly name: string;
    readonly realName: string | null;
}

/** A group under the main name. */
export interface QuickGroup {
    readonly id: GroupId;
    readonly name: string;
}

/** The sections of the quick search (docs/domain/search.md#quick-search). */
export interface QuickSearchResult {
    readonly software: QuickSection<Software>;
    readonly authors: QuickSection<QuickAuthor>;
    readonly groups: QuickSection<QuickGroup>;
}
