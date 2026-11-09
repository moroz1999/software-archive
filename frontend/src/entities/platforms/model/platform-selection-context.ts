import {createRequiredContext} from '@/shared/lib/context';
import type {PlatformCode} from './platform';

export interface PlatformSelection {
    /** The chosen platform, null for all of them. */
    readonly platform: PlatformCode | null;
    /** Chooses a platform for the whole archive, null for all of them. */
    readonly select: (platform: PlatformCode | null) => void;
}

/** The platform chosen with the switch, for everything inside a PlatformSelectionProvider. */
export const [PlatformSelectionContext, usePlatformSelection] = createRequiredContext<PlatformSelection>('PlatformSelection');
