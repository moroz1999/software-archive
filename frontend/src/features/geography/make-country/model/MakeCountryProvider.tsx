import type {ReactNode} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {GEO_QUERY_KEY, type GeographyCity, turnCityIntoCountry} from '@/entities/locations';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {makeCountryMessages} from '../config/messages';
import {MakeCountryContext, type MakeCountryState} from './make-country-context';

export interface MakeCountryProviderProps {
    readonly city: GeographyCity;
    readonly children: ReactNode;
}

/** Making a city of no known country a country: the lists of places shown are read again once it is one. */
export function MakeCountryProvider({city, children}: MakeCountryProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, isSuccess, error} = useMutation({
        mutationFn: () => turnCityIntoCountry(city.id),
        onSuccess: () => queryClient.invalidateQueries({queryKey: GEO_QUERY_KEY}),
    });
    const businessError = businessErrorOf(error);

    const state: MakeCountryState = {
        city,
        make: () => {
            mutate();
        },
        making: isPending,
        made: isSuccess,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, makeCountryMessages(t)),
    };

    return <MakeCountryContext value={state}>{children}</MakeCountryContext>;
}
