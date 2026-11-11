import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {createGroup, GROUP_QUERY_KEY, GroupFormProvider, type GroupId} from '@/entities/groups';
import {businessErrorOf} from '@/shared/api';
import {createGroupMessages} from '../config/messages';

export interface CreateGroupProviderProps {
    readonly onCreated: (id: GroupId) => void;
    readonly children: ReactNode;
}

/** Adding a group: the form inside sends its draft here; the archive's group lists go stale once it is added. */
export function CreateGroupProvider({onCreated, children}: CreateGroupProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, error} = useMutation({
        mutationFn: createGroup,
        onSuccess: async (id) => {
            await queryClient.invalidateQueries({queryKey: GROUP_QUERY_KEY, refetchType: 'none'});
            onCreated(id);
        },
    });

    return (
        <GroupFormProvider onSubmit={mutate} busy={isPending} error={businessErrorOf(error)} messages={createGroupMessages(t)}>
            {children}
        </GroupFormProvider>
    );
}
