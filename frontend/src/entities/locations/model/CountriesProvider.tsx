import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {countriesQuery} from '../api/geo-queries';
import {CountriesContext} from './countries-context';

/** Loads the countries to choose from for the blocks inside. */
export function CountriesProvider({children}: {readonly children: ReactNode}) {
    const query = useQuery(countriesQuery());
    return <CountriesContext value={query}>{children}</CountriesContext>;
}
