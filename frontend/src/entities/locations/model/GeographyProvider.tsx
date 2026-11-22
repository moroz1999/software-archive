import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {geographyQuery} from '../api/geo-queries';
import {GeographyContext} from './geography-context';

/** Loads every country with its cities for the blocks inside; takes over the call its route preloaded. */
export function GeographyProvider({children}: {readonly children: ReactNode}) {
    const query = useQuery(geographyQuery());
    return <GeographyContext value={query}>{children}</GeographyContext>;
}
