import type {AuthorId} from '@/entities/authors';

/** An entry of the catalog: a person under their main name, or one of their other labels with the main name. */
export interface AuthorCatalogEntry {
    readonly personId: AuthorId;
    readonly name: string;
    /** The real name of the person, shown next to the entry; null when unknown or when it is the entry's name. */
    readonly realName: string | null;
    readonly labelOf: string | null;
}
