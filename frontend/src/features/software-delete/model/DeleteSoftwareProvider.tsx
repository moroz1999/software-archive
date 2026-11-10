import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {deleteSoftware, SOFTWARE_QUERY_KEY, type Software} from '@/entities/software';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {deleteSoftwareMessages} from '../config/messages';
import {DeleteSoftwareContext, type DeleteSoftwareState} from './delete-software-context';

export interface DeleteSoftwareProviderProps {
    readonly software: Software;
    readonly onDeleted: () => void;
    readonly children: ReactNode;
}

/** Deleting software: the archive's software lists go stale once it is gone. */
export function DeleteSoftwareProvider({software, onDeleted, children}: DeleteSoftwareProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, isSuccess, error} = useMutation({
        mutationFn: () => deleteSoftware(software.id),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: SOFTWARE_QUERY_KEY, refetchType: 'none'});
            onDeleted();
        },
    });
    const businessError = businessErrorOf(error);

    const state: DeleteSoftwareState = {
        software,
        remove: () => {
            mutate();
        },
        deleting: isPending,
        deleted: isSuccess,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, deleteSoftwareMessages(t)),
    };

    return <DeleteSoftwareContext value={state}>{children}</DeleteSoftwareContext>;
}
