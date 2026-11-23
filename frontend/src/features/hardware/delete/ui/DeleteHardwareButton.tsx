import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {type HardwareDetails, hardwareName} from '@/entities/hardware';
import {parseUiLanguage} from '@/shared/lib/i18n';
import {Button} from '@/shared/ui/button';
import {ConfirmDialog} from '@/shared/ui/confirm-dialog';
import {useDeleteHardware} from '../model/delete-hardware-context';
import {DeleteHardwareProvider} from '../model/DeleteHardwareProvider';

export interface DeleteHardwareButtonProps {
    readonly hardware: HardwareDetails;
    readonly onDeleted: () => void;
}

/** Deletes a group or an item of hardware after asking; the archive refuses a group with hardware and hardware in use. */
export function DeleteHardwareButton({hardware, onDeleted}: DeleteHardwareButtonProps) {
    return (
        <DeleteHardwareProvider hardware={hardware} onDeleted={onDeleted}>
            <DeleteWithConfirmation/>
        </DeleteHardwareProvider>
    );
}

function DeleteWithConfirmation() {
    const {t, i18n} = useTranslation();
    const {hardware, remove, deleting, deleted, error} = useDeleteHardware();
    // Whether the confirmation is open is the dialog's own UI state, not business state.
    const [asking, setAsking] = useState(false);

    return (
        <>
            <Button color="danger" onClick={() => {
                setAsking(true);
            }}>
                {t('actions.delete')}
            </Button>
            <ConfirmDialog
                open={asking && !deleted}
                title={t(hardware.kind === 'group' ? 'hardware.deleteGroup' : 'hardware.deleteHardware')}
                confirmLabel={t('actions.deleteForGood')}
                onConfirm={remove}
                onCancel={() => {
                    setAsking(false);
                }}
                busy={deleting}
                error={error}
            >
                {t('hardware.deleteQuestion', {name: hardwareName(hardware.names, parseUiLanguage(i18n.language) ?? 'en')})}
            </ConfirmDialog>
        </>
    );
}
