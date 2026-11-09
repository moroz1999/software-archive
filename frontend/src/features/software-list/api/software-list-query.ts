import {queryOptions} from '@tanstack/react-query';
import {fetchSoftwarePage, SOFTWARE_QUERY_KEY} from '@/entities/software';

/** The first page of the software list; the route preloads it when the list is at the top of the view. */
export function softwareListQuery() {
    return queryOptions({
        queryKey: [...SOFTWARE_QUERY_KEY, 'list', 0],
        queryFn: ({signal}) => fetchSoftwarePage(0, signal),
    });
}
