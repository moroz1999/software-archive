import type {AuthorId} from '@/entities/authors';
import type {GroupId} from '@/entities/groups';
import type {Software} from '@/entities/software';
import type {Page} from '@/shared/api';

/** A person found by the search, under the main name; the real name when known and not the main name. */
export interface FoundAuthor {
    readonly id: AuthorId;
    readonly name: string;
    readonly realName: string | null;
}

/** A group found by the search, under the main name. */
export interface FoundGroup {
    readonly id: GroupId;
    readonly name: string;
}

/** The kinds of what the search finds, in the order they are listed (docs/domain/search.md#full-search). */
export const FOUND_KINDS = ['author', 'group', 'software'] as const;

export type FoundKind = (typeof FOUND_KINDS)[number];

/** The matches of one kind on a page of the full search, with the count of all matches of the kind. */
export type FoundSet =
    | {readonly kind: 'author'; readonly total: number; readonly items: readonly FoundAuthor[]}
    | {readonly kind: 'group'; readonly total: number; readonly items: readonly FoundGroup[]}
    | {readonly kind: 'software'; readonly total: number; readonly items: readonly Software[]};

/** A page of the full search: the matches of every kind on it, and the page of the one list. */
export interface FullSearchPage {
    readonly sets: readonly FoundSet[];
    readonly page: Omit<Page<never>, 'items'>;
}
