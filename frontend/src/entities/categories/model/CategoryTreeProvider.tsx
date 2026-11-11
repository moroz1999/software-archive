import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {categoryTreeQuery} from '../api/category-queries';
import {CategoryTreeContext} from './category-tree-context';

/** Loads the whole tree of categories for the blocks inside; takes over the call its route preloaded. */
export function CategoryTreeProvider({children}: {readonly children: ReactNode}) {
    const query = useQuery(categoryTreeQuery());
    return <CategoryTreeContext value={query}>{children}</CategoryTreeContext>;
}
