import type {UseQueryResult} from '@tanstack/react-query';
import type {CategoryNode} from '@/entities/categories';
import type {Country} from '@/entities/locations';
import {createRequiredContext} from '@/shared/lib/context';
import type {SoftwareCatalogPage} from './catalog';
import type {SoftwareCatalogFilter} from './catalog-filter';

export interface SoftwareCatalogState {
    /** What the address asks the catalog to show. */
    readonly filter: SoftwareCatalogFilter;
    readonly catalog: UseQueryResult<SoftwareCatalogPage>;
    /** The tree the categories of the filter are titled and nested by. */
    readonly categories: UseQueryResult<readonly CategoryNode[]>;
    /** The countries the country filter is named by. */
    readonly countries: UseQueryResult<readonly Country[]>;
    /** Another filter or page has been loading over the shown cards for a while: the controls are locked. */
    readonly changing: boolean;
    /** Goes to the address of another filter. */
    readonly show: (filter: SoftwareCatalogFilter) => void;
}

/** The catalog's filter and queries, for the blocks inside a SoftwareCatalogProvider. */
export const [SoftwareCatalogContext, useSoftwareCatalog] = createRequiredContext<SoftwareCatalogState>('SoftwareCatalog');
