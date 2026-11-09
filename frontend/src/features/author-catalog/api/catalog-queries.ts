import {keepPreviousData, queryOptions} from '@tanstack/react-query';
import type {PlatformCode} from '@/entities/platforms';
import {AUTHOR_QUERY_KEY} from '@/entities/authors';
import type {AuthorCatalogFilter} from '../model/catalog-filter';
import {fetchAuthorCatalogPage} from './catalog-api';

/**
 * A page of the catalog, of the chosen platform; another filter, page or platform keeps the shown one until its own
 * arrives.
 */
export function authorCatalogQuery(filter: AuthorCatalogFilter, platform: PlatformCode | null) {
    return queryOptions({
        queryKey: [...AUTHOR_QUERY_KEY, 'catalog', filter, platform],
        queryFn: ({signal}) => fetchAuthorCatalogPage(filter, platform, signal),
        placeholderData: keepPreviousData,
    });
}
