import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {City} from './geo';

/** The cities of one country to choose from, for the blocks inside a CitiesProvider. */
export const [CitiesContext, useCities] = createRequiredContext<UseQueryResult<readonly City[]>>('Cities');
