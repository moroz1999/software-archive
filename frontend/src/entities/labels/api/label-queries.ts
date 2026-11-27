import {queryOptions} from '@tanstack/react-query';
import {fetchLabel} from './label-api';

/** The common prefix of every label query. */
export const LABEL_QUERY_KEY = ['label'] as const;

/** One label with its details, the block at the top of its page. */
export function labelDetailsQuery(id: string) {
    return queryOptions({
        queryKey: [...LABEL_QUERY_KEY, 'details', id],
        queryFn: ({signal}) => fetchLabel(id, signal),
    });
}
