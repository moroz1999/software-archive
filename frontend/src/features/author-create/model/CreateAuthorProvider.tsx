import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {AUTHOR_QUERY_KEY, type AuthorId, AuthorFormProvider, createAuthor} from '@/entities/authors';
import {businessErrorOf} from '@/shared/api';
import {createAuthorMessages} from '../config/messages';

export interface CreateAuthorProviderProps {
    readonly onCreated: (id: AuthorId) => void;
    readonly children: ReactNode;
}

/** Adding an author: the form inside sends its draft here; the archive's author lists go stale once they are added. */
export function CreateAuthorProvider({onCreated, children}: CreateAuthorProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, error} = useMutation({
        mutationFn: createAuthor,
        onSuccess: async (id) => {
            await queryClient.invalidateQueries({queryKey: AUTHOR_QUERY_KEY, refetchType: 'none'});
            onCreated(id);
        },
    });

    return (
        <AuthorFormProvider onSubmit={mutate} busy={isPending} error={businessErrorOf(error)} messages={createAuthorMessages(t)}>
            {children}
        </AuthorFormProvider>
    );
}
