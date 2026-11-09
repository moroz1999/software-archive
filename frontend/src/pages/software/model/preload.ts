import type {QueryClient} from '@tanstack/react-query';
import {readSelectedPlatform} from '@/entities/platforms';
import {softwareListQuery} from '@/features/software-list';
import {preloadView} from '@/shared/api';

/** Route loader: the software list of the chosen platform is the block at the top of the page. */
export function preloadSoftwarePage(queryClient: QueryClient): () => Promise<null> {
    return () => preloadView([queryClient.query(softwareListQuery(readSelectedPlatform()))]);
}
