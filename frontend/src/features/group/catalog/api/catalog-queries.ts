import {keepPreviousData, queryOptions} from '@tanstack/react-query';
import type {PlatformCode} from '@/entities/platforms';
import {GROUP_QUERY_KEY} from '@/entities/groups';
import type {GroupCatalogFilter} from '../model/catalog-filter';
import {fetchGroupCatalogPage} from './catalog-api';

/**
 * A page of the catalog, of the chosen platform; another filter, page or platform keeps the shown one until its own
 * arrives.
 */
export function groupCatalogQuery(filter: GroupCatalogFilter, platform: PlatformCode | null) {
    return queryOptions({
        queryKey: [...GROUP_QUERY_KEY, 'catalog', filter, platform],
        queryFn: ({signal}) => fetchGroupCatalogPage(filter, platform, signal),
        placeholderData: keepPreviousData,
    });
}
