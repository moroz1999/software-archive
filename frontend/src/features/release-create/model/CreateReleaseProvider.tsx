import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {createRelease, type Hardware, type ReleaseDraft, ReleaseFormProvider, type ReleaseId, SOFTWARE_QUERY_KEY, type SoftwareDetails} from '@/entities/software';
import {businessErrorOf} from '@/shared/api';
import {createReleaseMessages} from '../config/messages';

export interface CreateReleaseProviderProps {
    readonly software: SoftwareDetails;
    /** The hardware of the software's platform, to choose from. */
    readonly hardware: readonly Hardware[];
    readonly onCreated: (id: ReleaseId) => void;
    readonly children: ReactNode;
}

/** Adding a release to software: the form inside sends its draft here; what shows the software goes stale once added. */
export function CreateReleaseProvider({software, hardware, onCreated, children}: CreateReleaseProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, error} = useMutation({
        mutationFn: (draft: ReleaseDraft) => createRelease(software.id, draft),
        onSuccess: async (id) => {
            await queryClient.invalidateQueries({queryKey: SOFTWARE_QUERY_KEY, refetchType: 'none'});
            onCreated(id);
        },
    });

    return (
        <ReleaseFormProvider hardware={hardware} onSubmit={mutate} busy={isPending} error={businessErrorOf(error)} messages={createReleaseMessages(t)}>
            {children}
        </ReleaseFormProvider>
    );
}
