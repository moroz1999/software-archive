import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {querySoftwareWithHardware, requireSoftwareId} from '@/entities/software';
import {preloadView} from '@/shared/api';

/** Route loader: the software and the hardware of its platform are the block at the top of the page; a malformed id is a page that does not exist. */
export function preloadReleaseNewPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return ({params}) => preloadView([querySoftwareWithHardware(queryClient, requireSoftwareId(params['id']))]);
}
