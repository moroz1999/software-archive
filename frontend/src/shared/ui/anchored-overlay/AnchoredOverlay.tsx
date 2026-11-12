import {type ReactNode, type RefObject, useLayoutEffect, useRef} from 'react';
import {createPortal} from 'react-dom';
import {classNames} from '@/shared/lib/class-names';
import styles from './AnchoredOverlay.module.css';

export interface AnchoredOverlayProps {
    readonly anchor: RefObject<HTMLElement | null>;
    readonly open: boolean;
    readonly children: ReactNode;
    readonly className?: string;
}

/** A portal positioned under an element, kept anchored while the viewport moves. */
export function AnchoredOverlay({anchor, open, children, className}: AnchoredOverlayProps) {
    const overlay = useRef<HTMLDivElement>(null);

    useLayoutEffect(() => {
        if (!open) {
            return undefined;
        }
        const position = () => {
            const anchorElement = anchor.current;
            const overlayElement = overlay.current;
            if (anchorElement === null || overlayElement === null) {
                return;
            }
            const rect = anchorElement.getBoundingClientRect();
            overlayElement.style.setProperty('--anchored-overlay-top', `${String(rect.bottom)}px`);
            overlayElement.style.setProperty('--anchored-overlay-left', `${String(rect.left)}px`);
            overlayElement.style.setProperty('--anchored-overlay-width', `${String(rect.width)}px`);
        };
        position();
        window.addEventListener('resize', position);
        window.addEventListener('scroll', position, true);
        return () => {
            window.removeEventListener('resize', position);
            window.removeEventListener('scroll', position, true);
        };
    }, [anchor, open]);

    if (!open) {
        return null;
    }
    return createPortal(<div ref={overlay} className={classNames(styles.overlay, className)}>{children}</div>, document.body);
}
