import {useState} from 'react';
import {deleteSoftware, type Software} from '../../../entities/software';
import {businessErrorMessage, useAction} from '../../../shared/api';
import {Button, ConfirmDialog} from '../../../shared/ui';
import {deleteSoftwareMessages} from '../config/messages';

export interface DeleteSoftwareButtonProps {
    readonly software: Software;
    readonly onDeleted: () => void;
}

/** Deletes software from the archive after asking. */
export function DeleteSoftwareButton({software, onDeleted}: DeleteSoftwareButtonProps) {
    const [asking, setAsking] = useState(false);
    const {state, run} = useAction(() => deleteSoftware(software.id), () => {
        setAsking(false);
        onDeleted();
    });

    return (
        <>
            <Button color="danger" onClick={() => {
                setAsking(true);
            }}>
                Delete
            </Button>
            <ConfirmDialog
                open={asking}
                title="Delete software"
                confirmLabel="Delete for good"
                onConfirm={run}
                onCancel={() => {
                    setAsking(false);
                }}
                busy={state.status === 'running'}
                error={state.status === 'business-error' ? businessErrorMessage(state.error, deleteSoftwareMessages) : undefined}
            >
                Delete “{software.title}” from the archive?
            </ConfirmDialog>
        </>
    );
}
