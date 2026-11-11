import type {UseQueryResult} from '@tanstack/react-query';
import type {City, Country} from '@/entities/locations';
import type {Page} from '@/shared/api';
import {createRequiredContext} from '@/shared/lib/context';
import type {AuthorCatalogEntry} from './catalog';
import type {AuthorCatalogFilter} from './catalog-filter';

export interface AuthorCatalogState {
    /** What the address asks the catalog to show. */
    readonly filter: AuthorCatalogFilter;
    readonly entries: UseQueryResult<Page<AuthorCatalogEntry>>;
    readonly countries: UseQueryResult<readonly Country[]>;
    /** The cities of the chosen country; pending while none is chosen. */
    readonly cities: UseQueryResult<readonly City[]>;
    /** Another filter or page has been loading over the shown entries for a while: the controls are locked. */
    readonly changing: boolean;
    /** Goes to the address of another filter. */
    readonly show: (filter: AuthorCatalogFilter) => void;
}

/** The catalog's filter and queries, for the blocks inside an AuthorCatalogProvider. */
export const [AuthorCatalogContext, useAuthorCatalog] = createRequiredContext<AuthorCatalogState>('AuthorCatalog');
