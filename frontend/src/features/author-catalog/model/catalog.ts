import type {AuthorId} from '@/entities/authors';
import type {CityId, CountryId} from './catalog-filter';

/** An entry of the catalog: a person under their main name, or one of their other aliases with the main name. */
export interface AuthorCatalogEntry {
    readonly personId: AuthorId;
    readonly name: string;
    /** The real name of the person, shown next to the entry; null when unknown or when it is the entry's name. */
    readonly realName: string | null;
    readonly aliasOf: string | null;
}

export interface Country {
    readonly id: CountryId;
    readonly name: string;
}

export interface City {
    readonly id: CityId;
    readonly name: string;
}
