import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {GROUP_QUERY_KEY, type GroupDetails, type GroupDraft, GroupFormProvider, toGroupFormValues, updateGroup} from '@/entities/groups';
import {businessErrorOf} from '@/shared/api';
import {editGroupMessages} from '../config/messages';

export interface EditGroupProviderProps {
    readonly group: GroupDetails;
    readonly onSaved: () => void;
    readonly children: ReactNode;
}

/** Saving changes of a group: the form inside starts from its values; what shows it goes stale once saved. */
export function EditGroupProvider({group, onSaved, children}: EditGroupProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, error} = useMutation({
        mutationFn: (draft: GroupDraft) => updateGroup(group.id, draft),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: GROUP_QUERY_KEY, refetchType: 'none'});
            onSaved();
        },
    });

    return (
        <GroupFormProvider
            initial={toGroupFormValues(group)}
            onSubmit={mutate}
            busy={isPending}
            error={businessErrorOf(error)}
            messages={editGroupMessages(t)}
        >
            {children}
        </GroupFormProvider>
    );
}
