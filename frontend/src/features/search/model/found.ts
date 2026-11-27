import {authorPath} from '@/entities/authors';
import {labelPath} from '@/entities/labels';
import type {components} from '@/shared/api';

type Schemas = components['schemas'];

/**
 * Found among the people: a person under the main name, with the real name when known and not the main name; or a
 * label of nobody under its name (docs/features/search.md#collections).
 */
export type FoundAuthor = Schemas['FoundAuthorDto'];

/** A person leads to their page, a label of nobody to its own. */
export function foundAuthorHref(author: FoundAuthor): string {
    return author.kind === 'person' ? authorPath(author.id) : labelPath(author.id);
}

/** A group found by the search, under the main name. */
export type FoundGroup = Schemas['FoundGroupDto'];

/** The kinds of what the search finds, in the order they are listed (docs/domain/search.md#full-search). */
export type FoundKind = Schemas['FoundKind'];

/** The matches of one kind on a page of the full search, with the count of all matches of the kind. */
export type FoundSet = Schemas['FoundSetDto'];

/** A page of the full search: the matches of every kind on it, and the page of the one list. */
export type FullSearchPage = Schemas['FullSearchDto'];
