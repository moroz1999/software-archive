import type {CategoryTitles} from '@/entities/categories';
import {createRequiredContext} from '@/shared/lib/context';

/** The category to delete, under its titles. */
export interface DeletedCategory {
    readonly id: string;
    readonly titles: CategoryTitles;
}

export interface DeleteCategoryState {
    readonly category: DeletedCategory;
    readonly remove: () => void;
    readonly deleting: boolean;
    readonly deleted: boolean;
    /** Why the category could not be deleted. */
    readonly error: string | undefined;
}

/** The deletion, for the controls inside a DeleteCategoryProvider. */
export const [DeleteCategoryContext, useDeleteCategory] = createRequiredContext<DeleteCategoryState>('DeleteCategory');
