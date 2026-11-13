import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {requireReleaseId, requireSoftwareId, softwareDetailsQuery, softwareReleasesQuery} from '@/entities/software';
import {preloadView} from '@/shared/api';

/** Route loader: the software and its releases form the block at the top; malformed ids mean the page does not exist. */
export function preloadReleaseViewPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return ({params}) => {
        const softwareId = requireSoftwareId(params['id']);
        requireReleaseId(params['releaseId']);
        return preloadView([
            queryClient.query(softwareDetailsQuery(softwareId)),
            queryClient.query(softwareReleasesQuery(softwareId)),
        ]);
    };
}
