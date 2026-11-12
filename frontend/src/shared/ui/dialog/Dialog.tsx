import {type ReactNode, useEffect, useId, useRef} from 'react';
import {createPortal} from 'react-dom';
import {Inline} from '../inline';
import {Stack} from '../stack';
import {Heading} from '../typography';
import styles from './Dialog.module.css';

export interface DialogProps {
    readonly open: boolean;
    readonly title: string;
    readonly onClose: () => void;
    readonly children: ReactNode;
    /** Buttons at the bottom, in reading order. */
    readonly actions?: ReactNode;
    /** `alertdialog` for messages that interrupt the user, such as errors. */
    readonly role?: 'dialog' | 'alertdialog';
    /** Opens with the focus in its first field instead of on the window, for a window that is a field above all. */
    readonly focusField?: boolean;
}

/** Modal dialog rendered over the page; closes on Escape. */
export function Dialog({open, title, onClose, children, actions, role = 'dialog', focusField = false}: DialogProps) {
    const titleId = useId();
    const ref = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (!open) {
            return undefined;
        }
        const field = focusField ? ref.current?.querySelector<HTMLElement>('input, select, textarea') : null;
        (field ?? ref.current)?.focus();
        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key === 'Escape') {
                onClose();
            }
        };
        document.addEventListener('keydown', onKeyDown);
        return () => {
            document.removeEventListener('keydown', onKeyDown);
        };
    }, [open, onClose, focusField]);

    if (!open) {
        return null;
    }

    return createPortal(
        <div className={styles.backdrop}>
            <div ref={ref} role={role} aria-modal="true" aria-labelledby={titleId} tabIndex={-1} className={styles.dialog}>
                <Stack gap="lg">
                    <Heading level={2} id={titleId}>{title}</Heading>
                    {children}
                    {actions !== undefined && <Inline gap="sm" justify="end">{actions}</Inline>}
                </Stack>
            </div>
        </div>,
        document.body,
    );
}
