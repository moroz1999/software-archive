import {type ReactNode, useState} from 'react';
import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {GEO_QUERY_KEY, type GeographyCity, putCityInCountry} from '@/entities/locations';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {putInCountryMessages} from '../config/messages';
import {PutInCountryContext, type PutInCountryState} from './put-in-country-context';

export interface PutInCountryProviderProps {
    readonly city: GeographyCity;
    readonly children: ReactNode;
}

/** Putting a city of no known country in the country chosen: the lists of places shown are read again once it is there. */
export function PutInCountryProvider({city, children}: PutInCountryProviderProps) {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const [country, setCountry] = useState('');
    const {mutate, isPending, error, reset} = useMutation({
        mutationFn: (chosen: string) => putCityInCountry(city.id, chosen),
        onSuccess: () => queryClient.invalidateQueries({queryKey: GEO_QUERY_KEY}),
    });
    const businessError = businessErrorOf(error);

    const state: PutInCountryState = {
        city,
        country,
        choose: (chosen) => {
            setCountry(chosen);
            reset();
        },
        put: () => {
            mutate(country);
        },
        putting: isPending,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, putInCountryMessages(t)),
    };

    return <PutInCountryContext value={state}>{children}</PutInCountryContext>;
}
