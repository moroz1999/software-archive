import type {ReactNode} from 'react';
import {useQuery} from '@tanstack/react-query';
import {placeDetailsQuery} from '../api/geo-queries';
import type {CityId, CountryId} from './geo';
import type {PlaceKind} from './geography';
import {PlaceDetailsContext} from './geography-context';

export interface PlaceDetailsProviderProps {
    readonly kind: PlaceKind;
    readonly id: CountryId | CityId;
    readonly children: ReactNode;
}

/** Loads one country or city for the blocks inside; takes over the call its route preloaded. */
export function PlaceDetailsProvider({kind, id, children}: PlaceDetailsProviderProps) {
    const query = useQuery(placeDetailsQuery(kind, id));
    return <PlaceDetailsContext value={query}>{children}</PlaceDetailsContext>;
}
