import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {ConfirmDialog} from '@/shared/ui/confirm-dialog';
import {type DeletedLabel, useDeleteLabel} from '../model/delete-label-context';
import {DeleteLabelProvider} from '../model/DeleteLabelProvider';

export interface DeleteLabelButtonProps {
    readonly label: DeletedLabel;
    readonly onDeleted: () => void;
}

/** Deletes a label from the archive after asking; its credits and memberships go with it. */
export function DeleteLabelButton({label, onDeleted}: DeleteLabelButtonProps) {
    return (
        <DeleteLabelProvider label={label} onDeleted={onDeleted}>
            <DeleteWithConfirmation/>
        </DeleteLabelProvider>
    );
}

function DeleteWithConfirmation() {
    const {t} = useTranslation();
    const {label, remove, deleting, deleted, error} = useDeleteLabel();
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
                title={t('label.delete')}
                confirmLabel={t('actions.deleteForGood')}
                onConfirm={remove}
                onCancel={() => {
                    setAsking(false);
                }}
                busy={deleting}
                error={error}
            >
                {t('label.deleteQuestion', {name: label.name})}
            </ConfirmDialog>
        </>
    );
}
