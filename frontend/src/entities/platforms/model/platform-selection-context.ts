import {createRequiredContext} from '@/shared/lib/context';

export interface PlatformSelection {
    /** The chosen platform, null for all of them. */
    readonly platform: string | null;
    /** Chooses a platform for the whole archive, null for all of them. */
    readonly select: (platform: string | null) => void;
}

/** The platform chosen with the switch, for everything inside a PlatformSelectionProvider. */
export const [PlatformSelectionContext, usePlatformSelection] = createRequiredContext<PlatformSelection>('PlatformSelection');
