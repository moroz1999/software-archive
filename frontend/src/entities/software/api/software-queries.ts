import {type QueryClient, queryOptions} from '@tanstack/react-query';
import type {ReleaseId} from '../model/release';
import type {SoftwareId, SoftwarePlatformCode} from '../model/software';
import {fetchScreenshots} from './screenshot-api';
import {fetchPlatformHardware, fetchSoftwareRelease} from './release-api';
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

/** One release of the software as it states itself, the block of its editing. */
export function softwareReleaseQuery(softwareId: SoftwareId, releaseId: ReleaseId) {
    return queryOptions({
        queryKey: [...SOFTWARE_QUERY_KEY, 'release', softwareId, releaseId],
        queryFn: ({signal}) => fetchSoftwareRelease(softwareId, releaseId, signal),
    });
}

/** The screenshots of the software itself, or of its release when one is given: a block of their pages. */
export function screenshotsQuery(softwareId: SoftwareId, releaseId: ReleaseId | null) {
    return queryOptions({
        queryKey: [...SOFTWARE_QUERY_KEY, 'screenshots', softwareId, releaseId],
        queryFn: ({signal}) => fetchScreenshots(softwareId, releaseId, signal),
    });
}

/** The hardware of a platform, to choose the hardware of a release from. */
export function platformHardwareQuery(platform: SoftwarePlatformCode) {
    return queryOptions({
        queryKey: ['hardware', platform],
        queryFn: ({signal}) => fetchPlatformHardware(platform, signal),
    });
}

/** Loads the software and then the hardware of its platform: what the forms of software and of its releases need. */
export function querySoftwareWithHardware(queryClient: QueryClient, id: SoftwareId): Promise<unknown> {
    return queryClient.query(softwareDetailsQuery(id)).then((software) => queryClient.query(platformHardwareQuery(software.platform.code)));
}
