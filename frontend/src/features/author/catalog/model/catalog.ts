import type {components} from '@/shared/api';


/**
 * An entry of the catalog: a person under the real name with the main label next to it, or a label — of a person with the real
 * name next to it, else the main name, or of nobody (docs/domain/authors.md).
 */
export type AuthorCatalogEntry = PersonEntry | LabelCatalogEntry;

/** A person under the real name, leading to the person. */
export interface PersonEntry {
    readonly kind: 'person';
    readonly personId: string;
    readonly name: string;
    /** Shown next to the name: the main label; null without labels. */
    readonly note: string | null;
}

/** A label, the main one too, leading to its own page. */
export interface LabelCatalogEntry {
    readonly kind: 'label';
    readonly labelId: string;
    readonly name: string;
    /** Shown next to the name: whose it is; null for a label of nobody and for the main name itself. */
    readonly note: string | null;
}

/** A page of the catalog as the archive gives it, its entries told apart by kind. */
export type AuthorCatalogPage = Omit<components['schemas']['PagedModelAuthorCatalogEntryDto'], 'content'> & {readonly content: readonly AuthorCatalogEntry[]};
