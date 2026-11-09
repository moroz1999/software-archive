import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {createSoftware, SOFTWARE_QUERY_KEY, SoftwareFormProvider, type SoftwareId} from '@/entities/software';
import {businessErrorOf} from '@/shared/api';
import {createSoftwareMessages} from '../config/messages';

export interface CreateSoftwareProviderProps {
    readonly onCreated: (id: SoftwareId) => void;
    readonly children: ReactNode;
}

/** Adding software: the form inside sends its draft here; the archive's software lists go stale once it is added. */
export function CreateSoftwareProvider({onCreated, children}: CreateSoftwareProviderProps) {
    const queryClient = useQueryClient();
    const {mutate, isPending, error} = useMutation({
        mutationFn: createSoftware,
        onSuccess: async (id) => {
            await queryClient.invalidateQueries({queryKey: SOFTWARE_QUERY_KEY, refetchType: 'none'});
            onCreated(id);
        },
    });

    return (
        <SoftwareFormProvider
            onSubmit={mutate}
            busy={isPending}
            error={businessErrorOf(error)}
            messages={createSoftwareMessages}
        >
            {children}
        </SoftwareFormProvider>
    );
}
