import {keepPreviousData, queryOptions} from '@tanstack/react-query';
import {AUTHOR_QUERY_KEY} from '@/entities/authors';
import type {AuthorCatalogFilter} from '../model/catalog-filter';
import {fetchAuthorCatalogPage} from './catalog-api';

/** A page of the catalog; another filter or page keeps the shown one until its own arrives. */
export function authorCatalogQuery(filter: AuthorCatalogFilter) {
    return queryOptions({
        queryKey: [...AUTHOR_QUERY_KEY, 'catalog', filter],
        queryFn: ({signal}) => fetchAuthorCatalogPage(filter, signal),
        placeholderData: keepPreviousData,
    });
}
