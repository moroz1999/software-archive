import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {GeoDuplicates, GeographyCity, GeographyCountry, PlaceDetails} from './geography';

/** Every country with its cities, for the blocks inside a GeographyProvider. */
export const [GeographyContext, useGeography] = createRequiredContext<UseQueryResult<readonly GeographyCountry[]>>('Geography');

/** The details query of a country or a city, for the blocks inside a PlaceDetailsProvider. */
export const [PlaceDetailsContext, usePlaceDetails] = createRequiredContext<UseQueryResult<PlaceDetails>>('PlaceDetails');

/** The cities of no known country, for the blocks inside a CitiesOfNoCountryProvider. */
export const [CitiesOfNoCountryContext, useCitiesOfNoCountry] = createRequiredContext<UseQueryResult<readonly GeographyCity[]>>('CitiesOfNoCountry');

/** The countries and the cities that may be duplicates, for the blocks inside a GeoDuplicatesProvider. */
export const [GeoDuplicatesContext, useGeoDuplicates] = createRequiredContext<UseQueryResult<GeoDuplicates>>('GeoDuplicates');
