import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {
    type Hardware,
    type ReleaseDetails,
    type ReleaseDraft,
    ReleaseFormProvider,
    SOFTWARE_QUERY_KEY,
    type SoftwareDetails,
    toReleaseFormValues,
    updateRelease,
} from '@/entities/software';
import {businessErrorOf} from '@/shared/api';
import {editReleaseMessages} from '../config/messages';

export interface EditReleaseProviderProps {
    readonly software: SoftwareDetails;
    readonly release: ReleaseDetails;
    /** The hardware of the software's platform, to choose from. */
    readonly hardware: readonly Hardware[];
    readonly onSaved: () => void;
    readonly children: ReactNode;
}

/** Saving changes of a release: the form inside starts from its values; what shows the software goes stale once saved. */
export function EditReleaseProvider({software, release, hardware, onSaved, children}: EditReleaseProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, error} = useMutation({
        mutationFn: (draft: ReleaseDraft) => updateRelease(software.id, release.id, draft),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: SOFTWARE_QUERY_KEY, refetchType: 'none'});
            onSaved();
        },
    });

    return (
        <ReleaseFormProvider
            initial={toReleaseFormValues(release)}
            hardware={hardware}
            onSubmit={mutate}
            busy={isPending}
            error={businessErrorOf(error)}
            messages={editReleaseMessages(t)}
        >
            {children}
        </ReleaseFormProvider>
    );
}
