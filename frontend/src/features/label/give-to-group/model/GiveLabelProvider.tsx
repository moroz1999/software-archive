import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {AUTHOR_QUERY_KEY} from '@/entities/authors';
import {GROUP_QUERY_KEY, type GroupId, toGroupId} from '@/entities/groups';
import {LABEL_QUERY_KEY, type LabelId} from '@/entities/labels';
import {SOFTWARE_QUERY_KEY} from '@/entities/software';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {giveLabelMessages} from '../config/messages';
import {GiveLabelContext, type GiveLabelState, type GivenLabel} from './give-label-context';

export interface GiveLabelProviderProps {
    readonly label: GivenLabel;
    /** Sends the label to the group: merging it in or making it a label of the group. */
    readonly send: (label: LabelId, group: string) => Promise<void>;
    readonly onGiven: (group: GroupId) => void;
    readonly children: ReactNode;
}

/** Giving a label to a group: the group, the catalogs and the software read differently afterwards. */
export function GiveLabelProvider({label, send, onGiven, children}: GiveLabelProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, error, reset} = useMutation({
        mutationFn: (group: string) => send(label.id, group),
        onSuccess: async (_result, group) => {
            await Promise.all([LABEL_QUERY_KEY, AUTHOR_QUERY_KEY, GROUP_QUERY_KEY, SOFTWARE_QUERY_KEY].map((queryKey) =>
                queryClient.invalidateQueries({queryKey, refetchType: 'none'})));
            onGiven(toGroupId(group));
        },
    });
    const businessError = businessErrorOf(error);

    const state: GiveLabelState = {
        label,
        give: (group: string) => {
            mutate(group);
        },
        giving: isPending,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, giveLabelMessages(t)),
        reset,
    };

    return <GiveLabelContext value={state}>{children}</GiveLabelContext>;
}
