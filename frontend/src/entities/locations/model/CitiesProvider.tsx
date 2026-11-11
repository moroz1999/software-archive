import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {citiesQuery} from '../api/geo-queries';
import {CitiesContext} from './cities-context';
import type {CountryId} from './geo';

export interface CitiesProviderProps {
    /** Nothing is loaded while no country is chosen. */
    readonly country: CountryId | null;
    readonly children: ReactNode;
}

/** Loads the cities of a country to choose from for the blocks inside. */
export function CitiesProvider({country, children}: CitiesProviderProps) {
    const query = useQuery(citiesQuery(country));
    return <CitiesContext value={query}>{children}</CitiesContext>;
}
