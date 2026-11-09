import {type ReactNode, useMemo, useState} from 'react';
import type {PlatformCode} from './platform';
import {PlatformSelectionContext} from './platform-selection-context';
import {readSelectedPlatform, storeSelectedPlatform} from './stored-platform';

/**
 * The platform chosen for the whole archive (docs/domain/hardware.md#platform-switch): starts from the one kept in
 * the browser and keeps every new choice there.
 */
export function PlatformSelectionProvider({children}: {readonly children: ReactNode}) {
    const [platform, setPlatform] = useState(readSelectedPlatform);

    const state = useMemo(() => ({
        platform,
        select: (next: PlatformCode | null) => {
            storeSelectedPlatform(next);
            setPlatform(next);
        },
    }), [platform]);

    return <PlatformSelectionContext value={state}>{children}</PlatformSelectionContext>;
}
