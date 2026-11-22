import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {GEO_QUERY_KEY, type PlaceDetails, type PlaceDraft, PlaceFormProvider, toPlaceFormValues, updatePlace} from '@/entities/locations';
import {businessErrorOf} from '@/shared/api';
import {editPlaceMessages} from '../config/messages';

export interface EditPlaceProviderProps {
    readonly place: PlaceDetails;
    readonly onSaved: () => void;
    readonly children: ReactNode;
}

/** Saving changes of a country or a city: the form inside starts from its values; every list of places goes stale once saved. */
export function EditPlaceProvider({place, onSaved, children}: EditPlaceProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, error} = useMutation({
        mutationFn: (draft: PlaceDraft) => updatePlace(place.kind, place.id, draft),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: GEO_QUERY_KEY, refetchType: 'none'});
            onSaved();
        },
    });

    return (
        <PlaceFormProvider
            kind={place.kind}
            initial={toPlaceFormValues(place)}
            onSubmit={mutate}
            busy={isPending}
            error={businessErrorOf(error)}
            messages={editPlaceMessages(t)}
        >
            {children}
        </PlaceFormProvider>
    );
}
