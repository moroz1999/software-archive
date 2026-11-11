import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {categoryDetailsQuery} from '../api/category-queries';
import type {CategoryId} from './category';
import {CategoryDetailsContext} from './category-details-context';

export interface CategoryDetailsProviderProps {
    readonly id: CategoryId;
    readonly children: ReactNode;
}

/** Loads one category for the blocks inside; takes over the call its route preloaded. */
export function CategoryDetailsProvider({id, children}: CategoryDetailsProviderProps) {
    const query = useQuery(categoryDetailsQuery(id));
    return <CategoryDetailsContext value={query}>{children}</CategoryDetailsContext>;
}
