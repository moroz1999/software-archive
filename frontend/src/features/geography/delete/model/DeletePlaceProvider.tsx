import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {deletePlace, GEO_QUERY_KEY, type PlaceDetails} from '@/entities/locations';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {deletePlaceMessages} from '../config/messages';
import {DeletePlaceContext, type DeletePlaceState} from './delete-place-context';

export interface DeletePlaceProviderProps {
    readonly place: PlaceDetails;
    readonly onDeleted: () => void;
    readonly children: ReactNode;
}

/** Deleting a country or a city: every list of places goes stale once it is gone. */
export function DeletePlaceProvider({place, onDeleted, children}: DeletePlaceProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, isSuccess, error} = useMutation({
        mutationFn: () => deletePlace(place.kind, place.id),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: GEO_QUERY_KEY, refetchType: 'none'});
            onDeleted();
        },
    });
    const businessError = businessErrorOf(error);

    const state: DeletePlaceState = {
        place,
        remove: () => {
            mutate();
        },
        deleting: isPending,
        deleted: isSuccess,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, deletePlaceMessages(t)),
    };

    return <DeletePlaceContext value={state}>{children}</DeletePlaceContext>;
}
