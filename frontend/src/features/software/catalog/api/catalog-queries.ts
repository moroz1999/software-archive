import {keepPreviousData, queryOptions} from '@tanstack/react-query';
import type {PlatformCode} from '@/entities/platforms';
import {SOFTWARE_QUERY_KEY} from '@/entities/software';
import type {SoftwareCatalogFilter} from '../model/catalog-filter';
import {fetchSoftwareCatalog} from './catalog-api';

/**
 * A page of the catalog with the values of its filters, of the chosen platform; another filter, page or platform keeps
 * the shown one until its own arrives (docs/data-loading.md).
 */
export function softwareCatalogQuery(filter: SoftwareCatalogFilter, platform: PlatformCode | null) {
    return queryOptions({
        queryKey: [...SOFTWARE_QUERY_KEY, 'catalog', filter, platform],
        queryFn: ({signal}) => fetchSoftwareCatalog(filter, platform, signal),
        placeholderData: keepPreviousData,
    });
}
