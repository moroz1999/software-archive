import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {GROUP_QUERY_KEY, deleteGroup} from '@/entities/groups';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {deleteGroupMessages} from '../config/messages';
import {DeleteGroupContext, type DeleteGroupState, type DeletedGroup} from './delete-group-context';

export interface DeleteGroupProviderProps {
    readonly group: DeletedGroup;
    readonly onDeleted: () => void;
    readonly children: ReactNode;
}

/** Deleting a group: the archive's group lists go stale once it is gone. */
export function DeleteGroupProvider({group, onDeleted, children}: DeleteGroupProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, isSuccess, error} = useMutation({
        mutationFn: () => deleteGroup(group.id),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: GROUP_QUERY_KEY, refetchType: 'none'});
            onDeleted();
        },
    });
    const businessError = businessErrorOf(error);

    const state: DeleteGroupState = {
        group,
        remove: () => {
            mutate();
        },
        deleting: isPending,
        deleted: isSuccess,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, deleteGroupMessages(t)),
    };

    return <DeleteGroupContext value={state}>{children}</DeleteGroupContext>;
}
