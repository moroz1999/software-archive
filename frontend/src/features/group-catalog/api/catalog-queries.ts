import {keepPreviousData, queryOptions} from '@tanstack/react-query';
import {GROUP_QUERY_KEY} from '@/entities/groups';
import type {GroupCatalogFilter} from '../model/catalog-filter';
import {fetchGroupCatalogPage} from './catalog-api';

/** A page of the catalog; another filter or page keeps the shown one until its own arrives. */
export function groupCatalogQuery(filter: GroupCatalogFilter) {
    return queryOptions({
        queryKey: [...GROUP_QUERY_KEY, 'catalog', filter],
        queryFn: ({signal}) => fetchGroupCatalogPage(filter, signal),
        placeholderData: keepPreviousData,
    });
}
