import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {type ReleaseId, releaseFilesQuery, reparseReleaseFiles, type SoftwareId} from '@/entities/software';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {reparseReleaseFilesMessages} from '../config/messages';
import {ReparseReleaseFilesContext, type ReparseReleaseFilesState} from './reparse-release-files-context';

export interface ReparseReleaseFilesProviderProps {
    readonly softwareId: SoftwareId;
    readonly releaseId: ReleaseId;
    readonly children: ReactNode;
}

/** Parsing a release file again: its tree is loaded anew once it is built. */
export function ReparseReleaseFilesProvider({softwareId, releaseId, children}: ReparseReleaseFilesProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, error} = useMutation({
        mutationFn: () => reparseReleaseFiles(softwareId, releaseId),
        onSuccess: () => queryClient.invalidateQueries({queryKey: releaseFilesQuery(softwareId, releaseId).queryKey}),
    });
    const businessError = businessErrorOf(error);

    const state: ReparseReleaseFilesState = {
        reparse: () => {
            mutate();
        },
        reparsing: isPending,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, reparseReleaseFilesMessages(t)),
    };

    return <ReparseReleaseFilesContext value={state}>{children}</ReparseReleaseFilesContext>;
}
