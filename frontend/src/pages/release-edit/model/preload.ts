import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {querySoftwareWithHardware, requireReleaseId, requireSoftwareId, softwareReleaseQuery} from '@/entities/software';
import {preloadView} from '@/shared/api';

/**
 * Route loader: the software, the hardware of its platform and the release are the block at the top of the page; a
 * malformed id is a page that does not exist.
 */
export function preloadReleaseEditPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return ({params}) => {
        const id = requireSoftwareId(params['id']);
        const releaseId = requireReleaseId(params['releaseId']);
        return preloadView([querySoftwareWithHardware(queryClient, id), queryClient.query(softwareReleaseQuery(id, releaseId))]);
    };
}
