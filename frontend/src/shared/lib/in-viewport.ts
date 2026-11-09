import {useCallback, useState} from 'react';

/** How far ahead of the viewport a lazy block starts its call (docs/data-loading.md). */
export const LAZY_LOAD_MARGIN = '200px 0px';

/**
 * Whether the element has come within LAZY_LOAD_MARGIN of the viewport; once it has, it stays true.
 * Lazy blocks start their calls by it (docs/data-loading.md). Without IntersectionObserver it is true at once.
 */
export function useInViewport(): readonly [(element: Element | null) => (() => void) | undefined, boolean] {
    const [visible, setVisible] = useState(() => typeof IntersectionObserver === 'undefined');

    const ref = useCallback((element: Element | null) => {
        if (element === null || visible) {
            return undefined;
        }
        const observer = new IntersectionObserver((entries) => {
            if (entries.some((entry) => entry.isIntersecting)) {
                setVisible(true);
                observer.disconnect();
            }
        }, {rootMargin: LAZY_LOAD_MARGIN});
        observer.observe(element);
        return () => {
            observer.disconnect();
        };
    }, [visible]);

    return [ref, visible];
}
