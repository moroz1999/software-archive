import {keepPreviousData, queryOptions} from '@tanstack/react-query';
import type {FullSearchFilter} from '../model/full-search-filter';
import {fetchFullSearch} from './full-search-api';

/** A page of the full search, of the chosen platform; another page or choice keeps the shown one until its own arrives. */
export function fullSearchQuery(filter: FullSearchFilter, platform: string | null) {
    return queryOptions({
        queryKey: ['search', 'full', filter, platform],
        queryFn: ({signal}) => fetchFullSearch(filter, platform, signal),
        placeholderData: keepPreviousData,
    });
}
