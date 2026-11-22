import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {createPlace, GEO_QUERY_KEY, type PlaceDraft, PlaceFormProvider, type PlaceKind} from '@/entities/locations';
import {businessErrorOf} from '@/shared/api';
import {createPlaceMessages} from '../config/messages';

export interface CreatePlaceProviderProps {
    readonly kind: PlaceKind;
    readonly onCreated: () => void;
    readonly children: ReactNode;
}

/** Adding a country or a city: the form inside sends its draft here; every list of places goes stale once it is added. */
export function CreatePlaceProvider({kind, onCreated, children}: CreatePlaceProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, error} = useMutation({
        mutationFn: (draft: PlaceDraft) => createPlace(kind, draft),
        onSuccess: async () => {
            await queryClient.invalidateQueries({queryKey: GEO_QUERY_KEY, refetchType: 'none'});
            onCreated();
        },
    });

    return (
        <PlaceFormProvider kind={kind} onSubmit={mutate} busy={isPending} error={businessErrorOf(error)} messages={createPlaceMessages(t)}>
            {children}
        </PlaceFormProvider>
    );
}
