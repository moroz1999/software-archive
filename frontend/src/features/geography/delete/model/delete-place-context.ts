import type {PlaceDetails} from '@/entities/locations';
import {createRequiredContext} from '@/shared/lib/context';

export interface DeletePlaceState {
    readonly place: PlaceDetails;
    readonly remove: () => void;
    readonly deleting: boolean;
    readonly deleted: boolean;
    /** Why the country or the city could not be deleted. */
    readonly error: string | undefined;
}

/** The deletion, for the controls inside a DeletePlaceProvider. */
export const [DeletePlaceContext, useDeletePlace] = createRequiredContext<DeletePlaceState>('DeletePlace');
