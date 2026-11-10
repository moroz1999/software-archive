import {type ReactNode, useEffect, useRef, useState} from 'react';
import {createPortal} from 'react-dom';
import {useTranslation} from 'react-i18next';
import {classNames} from '@/shared/lib/class-names';
import {Spinner} from '../spinner';
import styles from './LoadingState.module.css';

export interface LoadingStateProps {
    readonly loading: boolean;
    readonly children: ReactNode;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

const TRACK_THROTTLE_MS = 50;

/** Centres the pane on the part of the content inside the viewport; hides it when there is none. */
function placeSpinner(content: HTMLElement, pane: HTMLElement) {
    const rect = content.getBoundingClientRect();
    const left = Math.max(rect.left, 0);
    const right = Math.min(rect.right, window.innerWidth);
    const top = Math.max(rect.top, 0);
    const bottom = Math.min(rect.bottom, window.innerHeight);
    if (right <= left || bottom <= top) {
        pane.classList.remove(styles.visible);
        return;
    }
    pane.style.setProperty('--loading-state-spinner-x', `${String((left + right) / 2)}px`);
    pane.style.setProperty('--loading-state-spinner-y', `${String((top + bottom) / 2)}px`);
    pane.classList.add(styles.visible);
}

/**
 * Marks displayed content as being refreshed: dims and blurs it, blocks interaction and,
 * after a short delay, shows a spinner over the part of it that is on screen.
 */
export function LoadingState({loading, children, className}: LoadingStateProps) {
    const {t} = useTranslation();
    const content = useRef<HTMLDivElement>(null);
    const pane = useRef<HTMLDivElement>(null);
    // The pane is created on the first load and kept: its fade-out is a CSS transition.
    const [hasPane, setHasPane] = useState(loading);
    if (loading && !hasPane) {
        setHasPane(true);
    }

    useEffect(() => {
        const contentElement = content.current;
        const paneElement = pane.current;
        if (!loading || contentElement === null || paneElement === null) {
            return undefined;
        }
        // Flush the transparent starting style, otherwise the first fade-in is skipped.
        paneElement.getBoundingClientRect();
        placeSpinner(contentElement, paneElement);

        let last = 0;
        const track = () => {
            const now = Date.now();
            if (now - last >= TRACK_THROTTLE_MS) {
                last = now;
                placeSpinner(contentElement, paneElement);
            }
        };
        window.addEventListener('scroll', track, {capture: true, passive: true});
        window.addEventListener('resize', track, {passive: true});
        return () => {
            window.removeEventListener('scroll', track, {capture: true});
            window.removeEventListener('resize', track);
            paneElement.classList.remove(styles.visible);
        };
    }, [loading, hasPane]);

    return (
        <>
            <div ref={content} aria-busy={loading} className={classNames(styles.content, loading && styles.loading, className)}>
                {children}
            </div>
            {hasPane && createPortal(
                <div ref={pane} data-testid="loading-state-spinner" aria-hidden={!loading} className={styles.pane}>
                    <Spinner size="lg" label={t('loading.refreshing')}/>
                </div>,
                document.body,
            )}
        </>
    );
}
