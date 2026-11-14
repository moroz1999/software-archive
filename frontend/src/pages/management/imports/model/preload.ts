import type {QueryClient} from '@tanstack/react-query';
import {lastImportRunQuery, vektorSectionsQuery} from '@/features/import/vektor';
import {preloadView} from '@/shared/api';

/** Route loader: the sections to import from and the report of the last run are the blocks of the page. */
export function preloadManagementImportsPage(queryClient: QueryClient): () => Promise<null> {
    return () => preloadView([queryClient.query(vektorSectionsQuery()), queryClient.query(lastImportRunQuery())]);
}
