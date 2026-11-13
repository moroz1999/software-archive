import {type ReactNode, useEffect, useRef} from 'react';
import {AnchoredOverlay} from '../anchored-overlay';
import {Button} from '../button';
import styles from './IconPopover.module.css';

export interface IconPopoverProps {
    /** Names the trigger and the window it opens. */
    readonly label: string;
    /** The icon of the trigger. */
    readonly icon: ReactNode;
    readonly open: boolean;
    /** Asked to open or to close: by the trigger, Escape or a press outside. */
    readonly onOpenChange: (open: boolean) => void;
    readonly children: ReactNode;
}

/** An icon button of the header with its window of contextual content, anchored under its right edge. */
export function IconPopover({label, icon, open, onOpenChange, children}: IconPopoverProps) {
    const anchor = useRef<HTMLSpanElement>(null);
    const popup = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (!open) {
            return undefined;
        }
        const onPointerDown = (event: PointerEvent) => {
            const target = event.target;
            if (!(target instanceof Node) || anchor.current?.contains(target) === true || popup.current?.contains(target) === true) {
                return;
            }
            onOpenChange(false);
        };
        const onKeyDown = (event: KeyboardEvent) => {
            if (event.key !== 'Escape') {
                return;
            }
            event.preventDefault();
            onOpenChange(false);
            anchor.current?.querySelector('button')?.focus();
        };
        document.addEventListener('pointerdown', onPointerDown);
        document.addEventListener('keydown', onKeyDown);
        return () => {
            document.removeEventListener('pointerdown', onPointerDown);
            document.removeEventListener('keydown', onKeyDown);
        };
    }, [open, onOpenChange]);

    return (
        <>
            <span ref={anchor} className={styles.anchor}>
                <Button
                    color="transparent"
                    square
                    ariaLabel={label}
                    ariaHasPopup="dialog"
                    ariaExpanded={open}
                    onClick={() => {
                        onOpenChange(!open);
                    }}
                >
                    {icon}
                </Button>
            </span>
            <AnchoredOverlay anchor={anchor} open={open} align="end">
                <div ref={popup} role="dialog" aria-label={label} className={styles.window}>
                    {children}
                </div>
            </AnchoredOverlay>
        </>
    );
}
