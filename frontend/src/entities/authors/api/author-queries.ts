import {queryOptions} from '@tanstack/react-query';
import {fetchAuthor} from './author-api';

/** The common prefix of every author query. */
export const AUTHOR_QUERY_KEY = ['author'] as const;

/** One author with their details, the block at the top of their page. */
export function authorDetailsQuery(id: string) {
    return queryOptions({
        queryKey: [...AUTHOR_QUERY_KEY, 'details', id],
        queryFn: ({signal}) => fetchAuthor(id, signal),
    });
}
