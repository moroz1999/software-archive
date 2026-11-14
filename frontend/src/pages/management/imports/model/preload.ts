import type {QueryClient} from '@tanstack/react-query';
import {vektorSectionsQuery} from '@/features/import/vektor';
import {preloadView} from '@/shared/api';

/** Route loader: the sections to import from are the form at the top of the page. */
export function preloadManagementImportsPage(queryClient: QueryClient): () => Promise<null> {
    return () => preloadView([queryClient.query(vektorSectionsQuery())]);
}
