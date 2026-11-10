import {useCallback, useSyncExternalStore} from 'react';

/** Phones: below 576px (docs/styles.md#breakpoints). */
export const MOBILE_QUERY = '(max-width: 575.98px)';

/** Whether the screen matches the media query now; follows it as the screen changes. */
export function useMediaQuery(query: string): boolean {
    const subscribe = useCallback((onChange: () => void) => {
        const list = globalThis.matchMedia(query);
        list.addEventListener('change', onChange);
        return () => {
            list.removeEventListener('change', onChange);
        };
    }, [query]);
    return useSyncExternalStore(subscribe, () => globalThis.matchMedia(query).matches);
}
