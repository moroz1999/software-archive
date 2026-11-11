import {useEffect, useState} from 'react';
import {SKELETON_MIN_MS} from './skeleton-timing';

/** The state of a block's query the skeleton follows. */
export interface SkeletonQuery {
    readonly isPending: boolean;
    readonly isFetching: boolean;
}

/** No call behind the skeleton; a call started less than SKELETON_MIN_MS ago; a call started longer ago. */
type Phase = 'idle' | 'early' | 'late';

/**
 * Whether the block shows its skeleton: while its query is pending, and once shown, for at least SKELETON_MIN_MS since
 * the call started, so it never flashes (docs/data-loading.md). Combine it with `isPending` to narrow the query.
 */
export function useSkeleton({isPending, isFetching}: SkeletonQuery): boolean {
    const [phase, setPhase] = useState<Phase>('idle');

    if (phase === 'idle' && isPending && isFetching) {
        setPhase('early');
    }
    if (phase === 'late' && !isPending) {
        setPhase('idle');
    }

    useEffect(() => {
        if (phase !== 'early') {
            return undefined;
        }
        const timer = setTimeout(() => {
            setPhase('late');
        }, SKELETON_MIN_MS);
        return () => {
            clearTimeout(timer);
        };
    }, [phase]);

    return isPending || phase === 'early';
}
