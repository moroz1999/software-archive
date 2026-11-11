import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {CATEGORY_QUERY_KEY, deleteCategory} from '@/entities/categories';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {deleteCategoryMessages} from '../config/messages';
import {DeleteCategoryContext, type DeleteCategoryState, type DeletedCategory} from './delete-category-context';

export interface DeleteCategoryProviderProps {
    readonly category: DeletedCategory;
    readonly onDeleted: () => void;
    readonly children: ReactNode;
}

/** Deleting a category with its subcategories: the tree goes stale once they are gone. */
export function DeleteCategoryProvider({category, onDeleted, children}: DeleteCategoryProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, isSuccess, error} = useMutation({
        mutationFn: () => deleteCategory(category.id),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: CATEGORY_QUERY_KEY, refetchType: 'none'});
            onDeleted();
        },
    });
    const businessError = businessErrorOf(error);

    const state: DeleteCategoryState = {
        category,
        remove: () => {
            mutate();
        },
        deleting: isPending,
        deleted: isSuccess,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, deleteCategoryMessages(t)),
    };

    return <DeleteCategoryContext value={state}>{children}</DeleteCategoryContext>;
}
