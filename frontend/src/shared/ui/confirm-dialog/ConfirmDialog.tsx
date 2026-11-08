import type {ReactNode} from 'react';
import {Button} from '../button';
import {Dialog} from '../dialog';
import {Text} from '../typography';

export interface ConfirmDialogProps {
    readonly open: boolean;
    readonly title: string;
    /** The question; says what will happen. */
    readonly children: ReactNode;
    /** Names the action: "Delete for good", never "OK". */
    readonly confirmLabel: string;
    readonly onConfirm: () => void;
    readonly onCancel: () => void;
    /** The action runs: both buttons are locked. */
    readonly busy?: boolean;
    /** A business error of the action, shown inside the dialog. */
    readonly error?: string | undefined;
}

/** Asks before a destructive action and shows how it went. */
export function ConfirmDialog({open, title, children, confirmLabel, onConfirm, onCancel, busy = false, error}: ConfirmDialogProps) {
    return (
        <Dialog
            open={open}
            title={title}
            role="alertdialog"
            onClose={busy ? noop : onCancel}
            actions={(
                <>
                    <Button color="secondary" disabled={busy} onClick={onCancel}>Cancel</Button>
                    <Button color="danger" loading={busy} onClick={onConfirm}>{confirmLabel}</Button>
                </>
            )}
        >
            <Text>{children}</Text>
            {error !== undefined && <Text role="alert" tone="danger">{error}</Text>}
        </Dialog>
    );
}

function noop() {
    // Escape does nothing while the action runs.
}
