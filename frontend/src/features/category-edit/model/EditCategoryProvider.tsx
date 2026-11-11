import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {
    CATEGORY_QUERY_KEY,
    type CategoryDetails,
    type CategoryDraft,
    CategoryFormProvider,
    toCategoryFormValues,
    updateCategory,
} from '@/entities/categories';
import {businessErrorOf} from '@/shared/api';
import {editCategoryMessages} from '../config/messages';

export interface EditCategoryProviderProps {
    readonly category: CategoryDetails;
    readonly onSaved: () => void;
    readonly children: ReactNode;
}

/** Saving changes of a category: the form inside starts from its values; the tree goes stale once saved. */
export function EditCategoryProvider({category, onSaved, children}: EditCategoryProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, error} = useMutation({
        mutationFn: (draft: CategoryDraft) => updateCategory(category.id, draft),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: CATEGORY_QUERY_KEY, refetchType: 'none'});
            onSaved();
        },
    });

    return (
        <CategoryFormProvider
            initial={toCategoryFormValues(category)}
            editing={category.id}
            onSubmit={mutate}
            busy={isPending}
            error={businessErrorOf(error)}
            messages={editCategoryMessages(t)}
        >
            {children}
        </CategoryFormProvider>
    );
}
