import {type ReactNode, useEffect, useId, useRef} from 'react';
import {createPortal} from 'react-dom';
import {useTranslation} from 'react-i18next';
import {Button} from '../button';
import {CloseIcon} from '../icon';
import {Inline} from '../inline';
import {Heading} from '../typography';
import styles from './Drawer.module.css';

export interface DrawerProps {
    readonly open: boolean;
    readonly title: string;
    readonly onClose: () => void;
    readonly children: ReactNode;
    /** Controls beside the title. */
    readonly header?: ReactNode;
    /** What stays at the bottom while the body scrolls. */
    readonly footer?: ReactNode;
}

/**
 * A panel at the right edge of the window, over the page, which stays usable while it is open: not modal, so the
 * focus stays where the user puts it. Closes by its cross and by Escape pressed inside it.
 */
export function Drawer({open, title, onClose, children, header, footer}: DrawerProps) {
    const {t} = useTranslation();
    const titleId = useId();
    const ref = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (!open) {
            return undefined;
        }
        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key === 'Escape' && event.target instanceof Node && ref.current?.contains(event.target) === true) {
                onClose();
            }
        };
        document.addEventListener('keydown', onKeyDown);
        return () => {
            document.removeEventListener('keydown', onKeyDown);
        };
    }, [open, onClose]);

    if (!open) {
        return null;
    }
    return createPortal(
        <div ref={ref} role="dialog" aria-labelledby={titleId} className={styles.drawer}>
            <div className={styles.header}>
                <Inline gap="sm" align="center" justify="between">
                    <Heading level={2} variant="title" id={titleId}>{title}</Heading>
                    <Inline gap="xs" align="center">
                        {header}
                        <Button color="transparent" size="sm" square ariaLabel={t('actions.close')} onClick={onClose}>
                            <CloseIcon/>
                        </Button>
                    </Inline>
                </Inline>
            </div>
            <div className={styles.body}>{children}</div>
            {footer !== undefined && <div className={styles.footer}>{footer}</div>}
        </div>,
        document.body,
    );
}
