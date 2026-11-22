import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {GEO_QUERY_KEY, mergePlace, type PlaceDetails} from '@/entities/locations';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {mergePlaceMessages} from '../config/messages';
import {MergePlaceContext, type MergePlaceState} from './merge-place-context';

export interface MergePlaceProviderProps {
    readonly place: PlaceDetails;
    readonly onMerged: () => void;
    readonly children: ReactNode;
}

/** Merging a country or a city into another: every list of places goes stale once it is gone. */
export function MergePlaceProvider({place, onMerged, children}: MergePlaceProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, error, reset} = useMutation({
        mutationFn: (into: string) => mergePlace(place.kind, place.id, into),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: GEO_QUERY_KEY, refetchType: 'none'});
            onMerged();
        },
    });
    const businessError = businessErrorOf(error);

    const state: MergePlaceState = {
        place,
        merge: (into) => {
            mutate(into);
        },
        merging: isPending,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, mergePlaceMessages(t)),
        reset,
    };

    return <MergePlaceContext value={state}>{children}</MergePlaceContext>;
}
