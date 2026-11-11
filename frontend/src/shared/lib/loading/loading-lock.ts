import {useEffect, useState} from 'react';

/** How long a reload of shown data may take before the block is locked under a spinner (docs/data-loading.md). */
export const LOCK_DELAY_MS = 100;

/**
 * Whether a block that shows data is locked by its reload: only once the reload has lasted LOCK_DELAY_MS, so a quick
 * answer swaps the data without the block flashing (docs/data-loading.md). Unlocks as soon as the reload ends.
 */
export function useLoadingLock(loading: boolean): boolean {
    const [late, setLate] = useState(false);
    if (!loading && late) {
        setLate(false);
    }

    useEffect(() => {
        if (!loading) {
            return undefined;
        }
        const timer = setTimeout(() => {
            setLate(true);
        }, LOCK_DELAY_MS);
        return () => {
            clearTimeout(timer);
        };
    }, [loading]);

    return loading && late;
}
