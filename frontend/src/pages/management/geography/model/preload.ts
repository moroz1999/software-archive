import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {geographyQuery} from '@/entities/locations';
import {preloadView} from '@/shared/api';

/** Route loader: the tree is the whole page. */
export function preloadManagementGeographyPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return () => preloadView([queryClient.query(geographyQuery())]);
}
