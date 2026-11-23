import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {hardwareGroupsQuery} from '@/entities/hardware';
import {platformsQuery} from '@/entities/platforms';
import {preloadView} from '@/shared/api';

/** Route loader: the groups and the platforms an item of hardware is put in are in its form. */
export function preloadHardwareNewPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return () => preloadView([queryClient.query(hardwareGroupsQuery()), queryClient.query(platformsQuery())]);
}
