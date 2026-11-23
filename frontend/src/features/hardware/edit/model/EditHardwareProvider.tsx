import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {
    type HardwareDetails,
    type HardwareDraft,
    HardwareFormProvider,
    type HardwareGroupItem,
    type HardwarePlatformChoice,
    toHardwareFormValues,
    updateHardware,
} from '@/entities/hardware';
import {businessErrorOf} from '@/shared/api';
import {editHardwareMessages} from '../config/messages';

export interface EditHardwareProviderProps {
    readonly hardware: HardwareDetails;
    /** The groups and the platforms to choose from; undefined while they load. */
    readonly groups?: readonly HardwareGroupItem[] | undefined;
    readonly platforms?: readonly HardwarePlatformChoice[] | undefined;
    readonly onSaved: () => void;
    readonly children: ReactNode;
}

/**
 * Saving changes of a group or of an item of hardware: the form inside starts from its values and keeps its code. Its
 * names show on software pages and in the catalog, so everything cached goes stale once it is saved.
 */
export function EditHardwareProvider({hardware, groups, platforms, onSaved, children}: EditHardwareProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, error} = useMutation({
        mutationFn: (draft: HardwareDraft) => updateHardware(hardware.kind, hardware.code, draft),
        onSuccess: async () => {
            await queryClient.invalidateQueries({refetchType: 'none'});
            onSaved();
        },
    });

    return (
        <HardwareFormProvider
            kind={hardware.kind}
            initial={toHardwareFormValues(hardware)}
            codeFixed
            groups={groups}
            platforms={platforms}
            onSubmit={mutate}
            busy={isPending}
            error={businessErrorOf(error)}
            messages={editHardwareMessages(t)}
        >
            {children}
        </HardwareFormProvider>
    );
}
