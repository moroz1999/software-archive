import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {deleteHardware, type HardwareDetails} from '@/entities/hardware';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {deleteHardwareMessages} from '../config/messages';
import {DeleteHardwareContext, type DeleteHardwareState} from './delete-hardware-context';

export interface DeleteHardwareProviderProps {
    readonly hardware: HardwareDetails;
    readonly onDeleted: () => void;
    readonly children: ReactNode;
}

/** Deleting a group or an item of hardware: everything cached goes stale once it is gone. */
export function DeleteHardwareProvider({hardware, onDeleted, children}: DeleteHardwareProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, isSuccess, error} = useMutation({
        mutationFn: () => deleteHardware(hardware.kind, hardware.code),
        onSuccess: async () => {
            await queryClient.invalidateQueries({refetchType: 'none'});
            onDeleted();
        },
    });
    const businessError = businessErrorOf(error);

    const state: DeleteHardwareState = {
        hardware,
        remove: () => {
            mutate();
        },
        deleting: isPending,
        deleted: isSuccess,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, deleteHardwareMessages(t)),
    };

    return <DeleteHardwareContext value={state}>{children}</DeleteHardwareContext>;
}
