import {keepPreviousData, queryOptions} from '@tanstack/react-query';
import type {PlatformCode} from '@/entities/platforms';
import {fetchLatestSoftware, SOFTWARE_QUERY_KEY} from '@/entities/software';

/** The software of the chosen platform most recently added to the archive, the block at the top of the home page. */
export function latestSoftwareQuery(platform: PlatformCode | null) {
    return queryOptions({
        queryKey: [...SOFTWARE_QUERY_KEY, 'latest', platform],
        queryFn: ({signal}) => fetchLatestSoftware(platform, signal),
        // Another platform keeps the shown software under the spinner until its own arrives (docs/data-loading.md).
        placeholderData: keepPreviousData,
    });
}
