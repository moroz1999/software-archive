import type {ReactNode} from 'react';
import {useMutation, useQuery, useQueryClient} from '@tanstack/react-query';
import {platformsQuery} from '@/entities/platforms';
import {
    SOFTWARE_QUERY_KEY,
    type SoftwareDetails,
    type SoftwareDraft,
    SoftwareFormProvider,
    toSoftwareFormValues,
    updateSoftware,
} from '@/entities/software';
import {businessErrorOf} from '@/shared/api';
import {editSoftwareMessages} from '../config/messages';

export interface EditSoftwareProviderProps {
    readonly software: SoftwareDetails;
    readonly onSaved: () => void;
    readonly children: ReactNode;
}

/** Saving changes of existing software: the form inside starts from its values; what shows it goes stale once saved. */
export function EditSoftwareProvider({software, onSaved, children}: EditSoftwareProviderProps) {
    const queryClient = useQueryClient();
    const {mutate, isPending, error} = useMutation({
        mutationFn: (draft: SoftwareDraft) => updateSoftware(software.id, draft),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: SOFTWARE_QUERY_KEY, refetchType: 'none'});
            onSaved();
        },
    });

    const platforms = useQuery(platformsQuery());

    return (
        <SoftwareFormProvider
            initial={toSoftwareFormValues(software)}
            platforms={platforms.data ?? []}
            onSubmit={mutate}
            busy={isPending}
            error={businessErrorOf(error)}
            messages={editSoftwareMessages}
        >
            {children}
        </SoftwareFormProvider>
    );
}
