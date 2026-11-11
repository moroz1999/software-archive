import {queryOptions} from '@tanstack/react-query';
import type {CategoryId} from '../model/category';
import {fetchCategory, fetchCategoryTree} from './category-api';

/** The common prefix of every category query. */
export const CATEGORY_QUERY_KEY = ['category'] as const;

/** The whole tree of categories. */
export function categoryTreeQuery() {
    return queryOptions({
        queryKey: [...CATEGORY_QUERY_KEY, 'tree'],
        queryFn: ({signal}) => fetchCategoryTree(signal),
    });
}

/** One category, the block of its form. */
export function categoryDetailsQuery(id: CategoryId) {
    return queryOptions({
        queryKey: [...CATEGORY_QUERY_KEY, 'details', id],
        queryFn: ({signal}) => fetchCategory(id, signal),
    });
}
