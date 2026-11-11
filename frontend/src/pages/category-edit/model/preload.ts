import type {QueryClient} from '@tanstack/react-query';
import type {LoaderFunctionArgs} from 'react-router';
import {categoryDetailsQuery, categoryTreeQuery, requireCategoryId} from '@/entities/categories';
import {preloadView} from '@/shared/api';

/** Route loader: the category and the tree its parent is chosen from make the form; a malformed id is a page that does not exist. */
export function preloadCategoryEditPage(queryClient: QueryClient): (args: LoaderFunctionArgs) => Promise<null> {
    return ({params}) => {
        const id = requireCategoryId(params['id']);
        return preloadView([queryClient.query(categoryDetailsQuery(id)), queryClient.query(categoryTreeQuery())]);
    };
}
