import type {QueryClient} from '@tanstack/react-query';
import {groupTypesQuery} from '@/entities/groups';
import {countriesQuery} from '@/entities/locations';
import {preloadView} from '@/shared/api';

/** Route loader: the group types and the countries to locate the group in come with the form. */
export function preloadGroupNewPage(queryClient: QueryClient): () => Promise<null> {
    return () => preloadView([queryClient.query(groupTypesQuery()), queryClient.query(countriesQuery())]);
}
