import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {CATEGORY_QUERY_KEY, type CategoryId, CategoryFormProvider, createCategory} from '@/entities/categories';
import {businessErrorOf} from '@/shared/api';
import {createCategoryMessages} from '../config/messages';

export interface CreateCategoryProviderProps {
    readonly onCreated: (id: CategoryId) => void;
    readonly children: ReactNode;
}

/** Adding a category: the form inside sends its draft here; the tree goes stale once it is added. */
export function CreateCategoryProvider({onCreated, children}: CreateCategoryProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, error} = useMutation({
        mutationFn: createCategory,
        onSuccess: async (id) => {
            await queryClient.invalidateQueries({queryKey: CATEGORY_QUERY_KEY, refetchType: 'none'});
            onCreated(id);
        },
    });

    return (
        <CategoryFormProvider onSubmit={mutate} busy={isPending} error={businessErrorOf(error)} messages={createCategoryMessages(t)}>
            {children}
        </CategoryFormProvider>
    );
}
