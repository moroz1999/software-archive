import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import type {ReleaseDetails, Software} from '@/entities/software';
import {Button} from '@/shared/ui/button';
import {ConfirmDialog} from '@/shared/ui/confirm-dialog';
import {useDeleteRelease} from '../model/delete-release-context';
import {DeleteReleaseProvider} from '../model/DeleteReleaseProvider';

export interface DeleteReleaseButtonProps {
    readonly software: Software;
    readonly release: ReleaseDetails;
    readonly onDeleted: () => void;
}

/** Deletes a release of software after asking. */
export function DeleteReleaseButton({software, release, onDeleted}: DeleteReleaseButtonProps) {
    return (
        <DeleteReleaseProvider software={software} release={release} onDeleted={onDeleted}>
            <DeleteWithConfirmation/>
        </DeleteReleaseProvider>
    );
}

function DeleteWithConfirmation() {
    const {t} = useTranslation();
    const {name, remove, deleting, deleted, error} = useDeleteRelease();
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
                title={t('release.delete')}
                confirmLabel={t('actions.deleteForGood')}
                onConfirm={remove}
                onCancel={() => {
                    setAsking(false);
                }}
                busy={deleting}
                error={error}
            >
                {t('release.deleteQuestion', {name})}
            </ConfirmDialog>
        </>
    );
}
