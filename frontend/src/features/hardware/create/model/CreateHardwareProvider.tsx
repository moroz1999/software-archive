import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {createHardware, type HardwareDraft, HardwareFormProvider, type HardwareGroupItem, type HardwareKind, type HardwarePlatformChoice} from '@/entities/hardware';
import {businessErrorOf} from '@/shared/api';
import {createHardwareMessages} from '../config/messages';

export interface CreateHardwareProviderProps {
    readonly kind: HardwareKind;
    /** The groups and the platforms an item of hardware is put in; undefined while they load. */
    readonly groups?: readonly HardwareGroupItem[] | undefined;
    readonly platforms?: readonly HardwarePlatformChoice[] | undefined;
    readonly onCreated: () => void;
    readonly children: ReactNode;
}

/**
 * Adding a group or an item of hardware: the form inside sends its draft here. Its names show on software pages and
 * in the catalog, so everything cached goes stale once it is added.
 */
export function CreateHardwareProvider({kind, groups, platforms, onCreated, children}: CreateHardwareProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, error} = useMutation({
        mutationFn: (draft: HardwareDraft) => createHardware(kind, draft),
        onSuccess: async () => {
            await queryClient.invalidateQueries({refetchType: 'none'});
            onCreated();
        },
    });

    return (
        <HardwareFormProvider
            kind={kind}
            groups={groups}
            platforms={platforms}
            onSubmit={mutate}
            busy={isPending}
            error={businessErrorOf(error)}
            messages={createHardwareMessages(t)}
        >
            {children}
        </HardwareFormProvider>
    );
}
