import type {QueryClient} from '@tanstack/react-query';
import {latestSoftwareQuery} from '@/features/software-latest';
import {preloadView} from '@/shared/api';

/** Route loader: the latest software is the block at the top of the page. */
export function preloadHomePage(queryClient: QueryClient): () => Promise<null> {
    return () => preloadView([queryClient.query(latestSoftwareQuery())]);
}
