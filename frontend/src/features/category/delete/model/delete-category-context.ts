import type {CategoryId} from '@/entities/categories';
import {createRequiredContext} from '@/shared/lib/context';

/** The category to delete, under its title. */
export interface DeletedCategory {
    readonly id: CategoryId;
    readonly title: string;
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
