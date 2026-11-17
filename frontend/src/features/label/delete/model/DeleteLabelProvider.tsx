import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {AUTHOR_QUERY_KEY} from '@/entities/authors';
import {GROUP_QUERY_KEY} from '@/entities/groups';
import {deleteLabel, LABEL_QUERY_KEY} from '@/entities/labels';
import {SOFTWARE_QUERY_KEY} from '@/entities/software';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {deleteLabelMessages} from '../config/messages';
import {DeleteLabelContext, type DeleteLabelState, type DeletedLabel} from './delete-label-context';

export interface DeleteLabelProviderProps {
    readonly label: DeletedLabel;
    readonly onDeleted: () => void;
    readonly children: ReactNode;
}

/** Deleting a label: its owner, the catalogs and the software it was credited on go stale once it is gone. */
export function DeleteLabelProvider({label, onDeleted, children}: DeleteLabelProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, isSuccess, error} = useMutation({
        mutationFn: () => deleteLabel(label.id),
        onSuccess: async () => {
            await Promise.all([LABEL_QUERY_KEY, AUTHOR_QUERY_KEY, GROUP_QUERY_KEY, SOFTWARE_QUERY_KEY].map((queryKey) =>
                queryClient.invalidateQueries({queryKey, refetchType: 'none'})));
            onDeleted();
        },
    });
    const businessError = businessErrorOf(error);

    const state: DeleteLabelState = {
        label,
        remove: () => {
            mutate();
        },
        deleting: isPending,
        deleted: isSuccess,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, deleteLabelMessages(t)),
    };

    return <DeleteLabelContext value={state}>{children}</DeleteLabelContext>;
}
