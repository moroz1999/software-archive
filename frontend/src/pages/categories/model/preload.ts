import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {categoryTreeQuery} from '@/entities/categories';
import {preloadView} from '@/shared/api';

/** Route loader: the tree is the whole page. */
export function preloadCategoriesPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return () => preloadView([queryClient.query(categoryTreeQuery())]);
}
