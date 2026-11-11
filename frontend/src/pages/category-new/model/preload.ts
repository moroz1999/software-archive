import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {categoryTreeQuery} from '@/entities/categories';
import {preloadView} from '@/shared/api';

/** Route loader: the tree the parent is chosen from is in the form at the top of the page. */
export function preloadCategoryNewPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return () => preloadView([queryClient.query(categoryTreeQuery())]);
}
