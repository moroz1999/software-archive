import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {geoDuplicatesQuery} from '../api/geo-queries';
import {GeoDuplicatesContext} from './geography-context';

/** Loads the countries and the cities that may be duplicates for the blocks inside. */
export function GeoDuplicatesProvider({children}: {readonly children: ReactNode}) {
    const query = useQuery(geoDuplicatesQuery());
    return <GeoDuplicatesContext value={query}>{children}</GeoDuplicatesContext>;
}
