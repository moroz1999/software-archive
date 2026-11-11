import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {ConfirmDialog} from '@/shared/ui/confirm-dialog';
import {type DeletedGroup, useDeleteGroup} from '../model/delete-group-context';
import {DeleteGroupProvider} from '../model/DeleteGroupProvider';

export interface DeleteGroupButtonProps {
    readonly group: DeletedGroup;
    readonly onDeleted: () => void;
}

/** Deletes a group from the archive after asking; its labels stay as labels of nobody. */
export function DeleteGroupButton({group, onDeleted}: DeleteGroupButtonProps) {
    return (
        <DeleteGroupProvider group={group} onDeleted={onDeleted}>
            <DeleteWithConfirmation/>
        </DeleteGroupProvider>
    );
}

function DeleteWithConfirmation() {
    const {t} = useTranslation();
    const {group, remove, deleting, deleted, error} = useDeleteGroup();
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
                title={t('group.delete')}
                confirmLabel={t('actions.deleteForGood')}
                onConfirm={remove}
                onCancel={() => {
                    setAsking(false);
                }}
                busy={deleting}
                error={error}
            >
                {t('group.deleteQuestion', {name: group.name})}
            </ConfirmDialog>
        </>
    );
}
