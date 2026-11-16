import {type AuthorId, authorPath} from '@/entities/authors';
import type {GroupId} from '@/entities/groups';
import {type LabelId, labelPath} from '@/entities/labels';
import type {Software} from '@/entities/software';
import type {Page} from '@/shared/api';

/**
 * Found among the people: a person under the main name, with the real name when known and not the main name; or a
 * label of nobody under its name (docs/features/search.md#collections).
 */
export type FoundAuthor =
    | {readonly kind: 'person'; readonly id: AuthorId; readonly name: string; readonly realName: string | null}
    | {readonly kind: 'label'; readonly id: LabelId; readonly name: string};

/** A person leads to their page, a label of nobody to its own. */
export function foundAuthorHref(author: FoundAuthor): string {
    return author.kind === 'person' ? authorPath(author.id) : labelPath(author.id);
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
