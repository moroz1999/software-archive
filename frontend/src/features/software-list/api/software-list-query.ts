import {keepPreviousData, queryOptions} from '@tanstack/react-query';
import type {PlatformCode} from '@/entities/platforms';
import {fetchSoftwarePage, SOFTWARE_QUERY_KEY} from '@/entities/software';

/**
 * The first page of the software list, of the chosen platform; the route preloads it when the list is at the top of
 * the view.
 */
export function softwareListQuery(platform: PlatformCode | null) {
    return queryOptions({
        queryKey: [...SOFTWARE_QUERY_KEY, 'list', platform, 0],
        queryFn: ({signal}) => fetchSoftwarePage(0, platform, signal),
        // Another platform keeps the shown software under the spinner until its own arrives (docs/data-loading.md).
        placeholderData: keepPreviousData,
    });
}
