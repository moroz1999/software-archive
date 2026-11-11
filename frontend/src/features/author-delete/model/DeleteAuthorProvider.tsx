import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {AUTHOR_QUERY_KEY, deleteAuthor} from '@/entities/authors';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {deleteAuthorMessages} from '../config/messages';
import {DeleteAuthorContext, type DeleteAuthorState, type DeletedAuthor} from './delete-author-context';

export interface DeleteAuthorProviderProps {
    readonly author: DeletedAuthor;
    readonly onDeleted: () => void;
    readonly children: ReactNode;
}

/** Deleting an author: the archive's author lists go stale once they are gone. */
export function DeleteAuthorProvider({author, onDeleted, children}: DeleteAuthorProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, isSuccess, error} = useMutation({
        mutationFn: () => deleteAuthor(author.id),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: AUTHOR_QUERY_KEY, refetchType: 'none'});
            onDeleted();
        },
    });
    const businessError = businessErrorOf(error);

    const state: DeleteAuthorState = {
        author,
        remove: () => {
            mutate();
        },
        deleting: isPending,
        deleted: isSuccess,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, deleteAuthorMessages(t)),
    };

    return <DeleteAuthorContext value={state}>{children}</DeleteAuthorContext>;
}
