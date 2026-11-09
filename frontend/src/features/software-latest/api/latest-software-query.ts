import {queryOptions} from '@tanstack/react-query';
import {fetchLatestSoftware, SOFTWARE_QUERY_KEY} from '@/entities/software';

/** The software most recently added to the archive, the block at the top of the home page. */
export function latestSoftwareQuery() {
    return queryOptions({
        queryKey: [...SOFTWARE_QUERY_KEY, 'latest'],
        queryFn: ({signal}) => fetchLatestSoftware(signal),
    });
}
