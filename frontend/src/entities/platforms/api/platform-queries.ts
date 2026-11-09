import {queryOptions} from '@tanstack/react-query';
import {fetchPlatforms} from './platform-api';

/** The platforms to choose from: the switch and the software form. */
export function platformsQuery() {
    return queryOptions({
        queryKey: ['platforms'],
        queryFn: ({signal}) => fetchPlatforms(signal),
    });
}
