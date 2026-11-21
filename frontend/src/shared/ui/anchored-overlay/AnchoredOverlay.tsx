import {type ReactNode, type RefObject, useLayoutEffect, useRef} from 'react';
import {createPortal} from 'react-dom';
import {classNames} from '@/shared/lib/class-names';
import styles from './AnchoredOverlay.module.css';

export interface AnchoredOverlayProps {
    readonly anchor: RefObject<HTMLElement | null>;
    readonly open: boolean;
    readonly children: ReactNode;
    /**
     * `stretch` (default): as wide as the anchor, from its left edge. `end`: as wide as its content, its right edge
     * under the anchor's right edge. `start`: as wide as its content, from the anchor's left edge.
     */
    readonly align?: 'stretch' | 'end' | 'start';
    readonly className?: string;
}

/**
 * A portal positioned under an element, kept anchored while the viewport moves. It is a manual popover shown in the
 * top layer, so it stays above the page and any window on it without a stacking order of its own.
 */
export function AnchoredOverlay({anchor, open, children, align = 'stretch', className}: AnchoredOverlayProps) {
    const overlay = useRef<HTMLDivElement>(null);

    useLayoutEffect(() => {
        if (open) {
            overlay.current?.showPopover();
        }
    }, [open]);

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
            overlayElement.style.setProperty('--anchored-overlay-right', `${String(document.documentElement.clientWidth - rect.right)}px`);
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
    return createPortal(<div ref={overlay} popover="manual" className={classNames(styles.overlay, align === 'end' && styles.end, align === 'start' && styles.start, className)}>{children}</div>, document.body);
}
