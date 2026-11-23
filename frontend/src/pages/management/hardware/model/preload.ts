import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {hardwareGroupsQuery} from '@/entities/hardware';
import {preloadView} from '@/shared/api';

/** Route loader: every group with its hardware. */
export function preloadManagementHardwarePage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return () => preloadView([queryClient.query(hardwareGroupsQuery())]);
}
