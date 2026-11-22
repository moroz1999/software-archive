import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {GeographyCountry, PlaceDetails} from './geography';

/** Every country with its cities, for the blocks inside a GeographyProvider. */
export const [GeographyContext, useGeography] = createRequiredContext<UseQueryResult<readonly GeographyCountry[]>>('Geography');

/** The details query of a country or a city, for the blocks inside a PlaceDetailsProvider. */
export const [PlaceDetailsContext, usePlaceDetails] = createRequiredContext<UseQueryResult<PlaceDetails>>('PlaceDetails');
