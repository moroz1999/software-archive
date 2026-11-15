import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {AUTHOR_QUERY_KEY} from '@/entities/authors';
import {GROUP_QUERY_KEY} from '@/entities/groups';
import {attachLabel, LABEL_QUERY_KEY, type LabelOwner, type LabelOwnerOption} from '@/entities/labels';
import {SOFTWARE_QUERY_KEY} from '@/entities/software';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {attachLabelMessages} from '../config/messages';
import {AttachLabelContext, type AttachLabelState, type AttachedLabel} from './attach-label-context';

export interface AttachLabelProviderProps {
    readonly label: AttachedLabel;
    readonly onAttached: (owner: LabelOwner) => void;
    readonly children: ReactNode;
}

/** Attaching a label of nobody to its owner: the owner, its names and its software read differently afterwards. */
export function AttachLabelProvider({label, onAttached, children}: AttachLabelProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, error, reset} = useMutation({
        mutationFn: ({owner, display}: {readonly owner: LabelOwner; readonly display: boolean}) => attachLabel(label.id, owner, display),
        onSuccess: async (_result, {owner}) => {
            await Promise.all([LABEL_QUERY_KEY, AUTHOR_QUERY_KEY, GROUP_QUERY_KEY, SOFTWARE_QUERY_KEY].map((queryKey) =>
                queryClient.invalidateQueries({queryKey, refetchType: 'none'})));
            onAttached(owner);
        },
    });
    const businessError = businessErrorOf(error);

    const state: AttachLabelState = {
        label,
        attach: (option: LabelOwnerOption, display: boolean) => {
            mutate({owner: {type: option.type, id: option.id}, display});
        },
        attaching: isPending,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, attachLabelMessages(t)),
        reset,
    };

    return <AttachLabelContext value={state}>{children}</AttachLabelContext>;
}
