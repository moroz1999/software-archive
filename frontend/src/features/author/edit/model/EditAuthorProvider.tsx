import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {AUTHOR_QUERY_KEY, type AuthorDetails, type AuthorDraft, AuthorFormProvider, toAuthorFormValues, updateAuthor} from '@/entities/authors';
import {businessErrorOf} from '@/shared/api';
import {editAuthorMessages} from '../config/messages';

export interface EditAuthorProviderProps {
    readonly author: AuthorDetails;
    readonly onSaved: () => void;
    readonly children: ReactNode;
}

/** Saving changes of an author: the form inside starts from their values; what shows them goes stale once saved. */
export function EditAuthorProvider({author, onSaved, children}: EditAuthorProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, error} = useMutation({
        mutationFn: (draft: AuthorDraft) => updateAuthor(author.id, draft),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: AUTHOR_QUERY_KEY, refetchType: 'none'});
            onSaved();
        },
    });

    return (
        <AuthorFormProvider
            initial={toAuthorFormValues(author)}
            onSubmit={mutate}
            busy={isPending}
            error={businessErrorOf(error)}
            messages={editAuthorMessages(t)}
        >
            {children}
        </AuthorFormProvider>
    );
}
