import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {AUTHOR_QUERY_KEY} from '@/entities/authors';
import {GROUP_QUERY_KEY} from '@/entities/groups';
import {LABEL_QUERY_KEY, type LabelOwner, makeAuthorOfLabel, makeGroupOfLabel} from '@/entities/labels';
import {SOFTWARE_QUERY_KEY} from '@/entities/software';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {makeOwnerMessages} from '../config/messages';
import {type MadeOwnerLabel, MakeOwnerContext, type MakeOwnerState} from './make-owner-context';

export interface MakeOwnerProviderProps {
    readonly label: MadeOwnerLabel;
    readonly onMade: (owner: LabelOwner) => void;
    readonly children: ReactNode;
}

/**
 * Making a new person or group of a label: the owner it leaves, the new one, the catalogs and the software read
 * differently afterwards.
 */
export function MakeOwnerProvider({label, onMade, children}: MakeOwnerProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, error, reset} = useMutation({
        mutationFn: (types: readonly string[] | null) => types === null ? makeAuthorOfLabel(label.id) : makeGroupOfLabel(label.id, types),
        onSuccess: async (owner) => {
            await Promise.all([LABEL_QUERY_KEY, AUTHOR_QUERY_KEY, GROUP_QUERY_KEY, SOFTWARE_QUERY_KEY].map((queryKey) =>
                queryClient.invalidateQueries({queryKey, refetchType: 'none'})));
            onMade(owner);
        },
    });
    const businessError = businessErrorOf(error);

    const state: MakeOwnerState = {
        label,
        makeAuthor: () => {
            mutate(null);
        },
        makeGroup: (types) => {
            mutate(types);
        },
        making: isPending,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, makeOwnerMessages(t)),
        reset,
    };

    return <MakeOwnerContext value={state}>{children}</MakeOwnerContext>;
}
