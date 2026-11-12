import {keepPreviousData, queryOptions} from '@tanstack/react-query';
import type {PlatformCode} from '@/entities/platforms';
import {fetchQuickSearch} from './quick-search-api';

/** The first matches of the keyword, of the chosen platform; another keyword keeps the shown ones until its own arrive. */
export function quickSearchQuery(keyword: string, platform: PlatformCode | null) {
    return queryOptions({
        queryKey: ['quick-search', keyword, platform],
        queryFn: ({signal}) => fetchQuickSearch(keyword, platform, signal),
        placeholderData: keepPreviousData,
    });
}
