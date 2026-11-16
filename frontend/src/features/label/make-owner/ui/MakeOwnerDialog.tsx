import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {Dialog} from '@/shared/ui/dialog';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {useMakeOwner} from '../model/make-owner-context';

export interface MakeOwnerDialogProps {
    readonly open: boolean;
    readonly title: string;
    readonly question: string;
    /** What else the new owner needs before it can be made. */
    readonly children?: ReactNode;
    readonly ready: boolean;
    readonly onMake: () => void;
    readonly onClose: () => void;
}

/** Asks before making an owner of the label, says whom it is taken away from and shows why it could not be done. */
export function MakeOwnerDialog({open, title, question, children, ready, onMake, onClose}: MakeOwnerDialogProps) {
    const {t} = useTranslation();
    const {label, making, error} = useMakeOwner();
    return (
        <Dialog
            open={open}
            title={title}
            onClose={making ? noop : onClose}
            actions={(
                <>
                    <Button color="secondary" disabled={making} onClick={onClose}>{t('actions.cancel')}</Button>
                    <Button disabled={!ready} loading={making} onClick={onMake}>{title}</Button>
                </>
            )}
        >
            <Stack gap="md">
                <Text>{question}</Text>
                {label.owner !== null && <Text tone="muted">{t('label.takenAway', {owner: label.owner.name})}</Text>}
                {children}
                {error !== undefined && <Text role="alert" tone="danger">{error}</Text>}
            </Stack>
        </Dialog>
    );
}

function noop() {
    // The dialog stays open while the owner is being made.
}
