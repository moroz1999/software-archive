import type {QueryClient} from '@tanstack/react-query';
import {countriesQuery} from '@/entities/locations';
import {preloadView} from '@/shared/api';

/** Route loader: the countries to locate the label in come with the form. */
export function preloadLabelNewPage(queryClient: QueryClient): () => Promise<null> {
    return () => preloadView([queryClient.query(countriesQuery())]);
}
