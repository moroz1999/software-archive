import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {hardwareDetailsQuery, hardwareGroupsQuery, type HardwareKind, requireHardwareCode} from '@/entities/hardware';
import {platformsQuery} from '@/entities/platforms';
import {preloadView} from '@/shared/api';

/** Route loader: the group or the item makes the form, an item's with the groups and the platforms to choose from; a malformed code is a page that does not exist. */
function preloadEditPage(kind: HardwareKind, queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return ({params}) => {
        const code = requireHardwareCode(kind, params['code']);
        const details = queryClient.query(hardwareDetailsQuery(kind, code));
        return preloadView(kind === 'hardware'
            ? [details, queryClient.query(hardwareGroupsQuery()), queryClient.query(platformsQuery())]
            : [details]);
    };
}

export function preloadHardwareGroupEditPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return preloadEditPage('group', queryClient);
}

export function preloadHardwareEditPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return preloadEditPage('hardware', queryClient);
}
