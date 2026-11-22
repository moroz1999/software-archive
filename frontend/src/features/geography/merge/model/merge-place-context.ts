import type {PlaceDetails} from '@/entities/locations';
import {createRequiredContext} from '@/shared/lib/context';

export interface MergePlaceState {
    readonly place: PlaceDetails;
    /** Merges the place into the one with this id, the one that stays. */
    readonly merge: (into: string) => void;
    readonly merging: boolean;
    /** Why the place could not be merged. */
    readonly error: string | undefined;
    /** Forgets the last refusal, when another is chosen or the dialog closes. */
    readonly reset: () => void;
}

/** Merging one country or city into another, for the controls inside a MergePlaceProvider. */
export const [MergePlaceContext, useMergePlace] = createRequiredContext<MergePlaceState>('MergePlace');
