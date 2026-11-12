import {type ReactNode, useEffect, useId, useRef} from 'react';
import {createPortal} from 'react-dom';
import {useTranslation} from 'react-i18next';
import {Button} from '../button';
import {CloseIcon} from '../icon';
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
    /** Closes by a click on the backdrop and by a cross next to the title too, for a window with nothing to lose. */
    readonly dismissible?: boolean;
    /** Takes the place of the title, such as a field the window is about; the title then only names it for assistive technology. */
    readonly header?: ReactNode;
}

/** Modal dialog rendered over the page; closes on Escape. */
export function Dialog({open, title, onClose, children, actions, role = 'dialog', focusField = false, dismissible = false, header}: DialogProps) {
    const {t} = useTranslation();
    const titleId = useId();
    const ref = useRef<HTMLDivElement>(null);
    const backdrop = useRef<HTMLDivElement>(null);

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

    // A click on the backdrop closes the window only when it started there too, so a selection dragged out of the
    // window keeps it open; the keyboard closes it by Escape.
    useEffect(() => {
        const element = backdrop.current;
        if (!open || !dismissible || element === null) {
            return undefined;
        }
        let pressedOnBackdrop = false;
        const onPointerDown = (event: PointerEvent) => {
            pressedOnBackdrop = event.target === element;
        };
        const onClick = (event: MouseEvent) => {
            if (pressedOnBackdrop && event.target === element) {
                onClose();
            }
        };
        element.addEventListener('pointerdown', onPointerDown);
        element.addEventListener('click', onClick);
        return () => {
            element.removeEventListener('pointerdown', onPointerDown);
            element.removeEventListener('click', onClick);
        };
    }, [open, dismissible, onClose]);

    if (!open) {
        return null;
    }

    return createPortal(
        <div ref={backdrop} data-testid="dialog-backdrop" className={styles.backdrop}>
            <div
                ref={ref}
                role={role}
                aria-modal="true"
                aria-labelledby={header === undefined ? titleId : undefined}
                aria-label={header === undefined ? undefined : title}
                tabIndex={-1}
                className={styles.dialog}
            >
                <Stack gap="lg">
                    {dismissible
                        ? (
                            <Inline gap="sm" align="center" justify="between">
                                {header === undefined ? <Heading level={2} id={titleId}>{title}</Heading> : <div className={styles.header}>{header}</div>}
                                <Button color="transparent" size="sm" square ariaLabel={t('actions.close')} onClick={onClose}>
                                    <CloseIcon/>
                                </Button>
                            </Inline>
                        )
                        : header ?? <Heading level={2} id={titleId}>{title}</Heading>}
                    {children}
                    {actions !== undefined && <Inline gap="sm" justify="end">{actions}</Inline>}
                </Stack>
            </div>
        </div>,
        document.body,
    );
}
