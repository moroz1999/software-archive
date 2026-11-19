import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {releaseFilesQuery, requireReleaseId, requireSoftwareId, softwareDetailsQuery, softwareReleasesQuery} from '@/entities/software';
import {preloadView} from '@/shared/api';

/** Route loader: the software and its releases form the block at the top; malformed ids mean the page does not exist. */
export function preloadReleaseViewPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return ({params}) => {
        const softwareId = requireSoftwareId(params['id']);
        const releaseId = requireReleaseId(params['releaseId']);
        const releases = queryClient.query(softwareReleasesQuery(softwareId));
        return preloadView([
            queryClient.query(softwareDetailsQuery(softwareId)),
            releases,
            releases.then((items) => {
                const release = items.find((item) => item.id === releaseId);
                return release?.archive == null ? null : queryClient.query(releaseFilesQuery(softwareId, releaseId));
            }),
        ]);
    };
}
