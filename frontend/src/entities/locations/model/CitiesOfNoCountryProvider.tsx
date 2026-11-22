import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {citiesOfNoCountryQuery} from '../api/geo-queries';
import {CitiesOfNoCountryContext} from './geography-context';

/** Loads the cities of no known country for the blocks inside; takes over the call its route preloaded. */
export function CitiesOfNoCountryProvider({children}: {readonly children: ReactNode}) {
    const query = useQuery(citiesOfNoCountryQuery());
    return <CitiesOfNoCountryContext value={query}>{children}</CitiesOfNoCountryContext>;
}
