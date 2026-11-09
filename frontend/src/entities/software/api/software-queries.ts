import {queryOptions} from '@tanstack/react-query';
import type {SoftwareId} from '../model/software';
import {fetchSoftware, fetchSoftwareReleases} from './software-api';

/** The common prefix of every software query: invalidating it refreshes all of them after a change. */
export const SOFTWARE_QUERY_KEY = ['software'] as const;

/** One software with its details, the block at the top of its page and of its editing. */
export function softwareDetailsQuery(id: SoftwareId) {
    return queryOptions({
        queryKey: [...SOFTWARE_QUERY_KEY, 'details', id],
        queryFn: ({signal}) => fetchSoftware(id, signal),
    });
}

/** The releases of one software, a block lower on its page. */
export function softwareReleasesQuery(id: SoftwareId) {
    return queryOptions({
        queryKey: [...SOFTWARE_QUERY_KEY, 'releases', id],
        queryFn: ({signal}) => fetchSoftwareReleases(id, signal),
    });
}
