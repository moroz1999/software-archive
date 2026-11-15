import type {AuthorId} from '@/entities/authors';
import type {LabelId} from '@/entities/labels';

/** An entry of the catalog: a person under their main name, one of their other labels with the main name, or a label of nobody. */
export type AuthorCatalogEntry = PersonEntry | NobodysLabelEntry;

/** A person under their main name, or one of their other labels with the main name. */
export interface PersonEntry {
    readonly kind: 'person';
    readonly personId: AuthorId;
    readonly name: string;
    /** The real name of the person, shown next to the entry; null when unknown or when it is the entry's name. */
    readonly realName: string | null;
    readonly labelOf: string | null;
}

/** A label whose owner is not known yet, leading to its own page. */
export interface NobodysLabelEntry {
    readonly kind: 'label';
    readonly labelId: LabelId;
    readonly name: string;
}
