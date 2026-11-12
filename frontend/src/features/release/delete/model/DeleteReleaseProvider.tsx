import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {deleteRelease, type ReleaseDetails, SOFTWARE_QUERY_KEY, type Software} from '@/entities/software';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {deleteReleaseMessages} from '../config/messages';
import {DeleteReleaseContext, type DeleteReleaseState} from './delete-release-context';

export interface DeleteReleaseProviderProps {
    readonly software: Software;
    readonly release: ReleaseDetails;
    readonly onDeleted: () => void;
    readonly children: ReactNode;
}

/** Deleting a release: what shows its software goes stale once it is gone. */
export function DeleteReleaseProvider({software, release, onDeleted, children}: DeleteReleaseProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, isSuccess, error} = useMutation({
        mutationFn: () => deleteRelease(software.id, release.id),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: SOFTWARE_QUERY_KEY, refetchType: 'none'});
            onDeleted();
        },
    });
    const businessError = businessErrorOf(error);
    const title = `“${release.title ?? software.title}”`;

    const state: DeleteReleaseState = {
        name: release.version === null ? title : `${title} ${release.version}`,
        remove: () => {
            mutate();
        },
        deleting: isPending,
        deleted: isSuccess,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, deleteReleaseMessages(t)),
    };

    return <DeleteReleaseContext value={state}>{children}</DeleteReleaseContext>;
}
