import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {CreditedRelease} from './credited-release';

/** The query of the releases an owner is credited on, for the blocks inside a CreditedReleasesProvider. */
export const [CreditedReleasesContext, useCreditedReleases] = createRequiredContext<UseQueryResult<readonly CreditedRelease[]>>('CreditedReleases');
