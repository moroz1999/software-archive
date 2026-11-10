import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import type {Software} from '@/entities/software';
import {Button} from '@/shared/ui/button';
import {ConfirmDialog} from '@/shared/ui/confirm-dialog';
import {useDeleteSoftware} from '../model/delete-software-context';
import {DeleteSoftwareProvider} from '../model/DeleteSoftwareProvider';

export interface DeleteSoftwareButtonProps {
    readonly software: Software;
    readonly onDeleted: () => void;
}

/** Deletes software from the archive after asking. */
export function DeleteSoftwareButton({software, onDeleted}: DeleteSoftwareButtonProps) {
    return (
        <DeleteSoftwareProvider software={software} onDeleted={onDeleted}>
            <DeleteWithConfirmation/>
        </DeleteSoftwareProvider>
    );
}

function DeleteWithConfirmation() {
    const {t} = useTranslation();
    const {software, remove, deleting, deleted, error} = useDeleteSoftware();
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
                title={t('software.delete')}
                confirmLabel={t('actions.deleteForGood')}
                onConfirm={remove}
                onCancel={() => {
                    setAsking(false);
                }}
                busy={deleting}
                error={error}
            >
                {t('software.deleteQuestion', {title: software.title})}
            </ConfirmDialog>
        </>
    );
}
