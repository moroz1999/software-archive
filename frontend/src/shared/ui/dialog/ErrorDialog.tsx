import {Button} from '../button';
import {Text} from '../typography';
import {Dialog} from './Dialog';

export interface ErrorDialogProps {
    readonly open: boolean;
    readonly title: string;
    readonly message: string;
    readonly onClose: () => void;
    /** Present only when the failed action can be repeated. */
    readonly onRetry?: () => void;
}

/** The system error modal (docs/error-handling.md). */
export function ErrorDialog({open, title, message, onClose, onRetry}: ErrorDialogProps) {
    const {t} = useTranslation();
    return (
        <Dialog
            open={open}
            title={title}
            onClose={onClose}
            role="alertdialog"
            actions={(
                <>
                    <Button color="secondary" onClick={onClose}>{t('actions.close')}</Button>
                    {onRetry !== undefined && <Button onClick={onRetry}>{t('actions.retry')}</Button>}
                </>
            )}
        >
            <Text>{message}</Text>
        </Dialog>
    );
}
import {useTranslation} from 'react-i18next';
