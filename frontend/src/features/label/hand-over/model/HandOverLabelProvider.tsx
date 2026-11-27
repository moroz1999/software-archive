import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {AUTHOR_QUERY_KEY} from '@/entities/authors';
import {GROUP_QUERY_KEY} from '@/entities/groups';
import {LABEL_QUERY_KEY} from '@/entities/labels';
import {SOFTWARE_QUERY_KEY} from '@/entities/software';
import {businessErrorMessage, businessErrorOf, type BusinessMessages} from '@/shared/api';
import {HandOverLabelContext, type HandOverLabelState, type HandedLabel} from './hand-over-context';

export interface HandOverLabelProviderProps {
    readonly label: HandedLabel;
    /** Sends the label to the group, the person or the other label of the id: merging it in or giving it away. */
    readonly send: (label: string, to: string) => Promise<void>;
    /** The texts of the refusals this handing over can receive. */
    readonly messages: BusinessMessages;
    readonly onHandedOver: (to: string) => void;
    readonly children: ReactNode;
}

/** Handing a label over: whoever takes it, the catalogs and the software read differently afterwards. */
export function HandOverLabelProvider({label, send, messages, onHandedOver, children}: HandOverLabelProviderProps) {
    const queryClient = useQueryClient();
    const {mutate, isPending, error, reset} = useMutation({
        mutationFn: (to: string) => send(label.id, to),
        onSuccess: async (_result, to) => {
            await Promise.all([LABEL_QUERY_KEY, AUTHOR_QUERY_KEY, GROUP_QUERY_KEY, SOFTWARE_QUERY_KEY].map((queryKey) =>
                queryClient.invalidateQueries({queryKey, refetchType: 'none'})));
            onHandedOver(to);
        },
    });
    const businessError = businessErrorOf(error);

    const state: HandOverLabelState = {
        label,
        handOver: (to: string) => {
            mutate(to);
        },
        handingOver: isPending,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, messages),
        reset,
    };

    return <HandOverLabelContext value={state}>{children}</HandOverLabelContext>;
}
