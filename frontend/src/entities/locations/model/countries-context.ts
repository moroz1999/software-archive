import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {Country} from './geo';

/** The countries to choose from, for the blocks inside a CountriesProvider. */
export const [CountriesContext, useCountries] = createRequiredContext<UseQueryResult<readonly Country[]>>('Countries');
