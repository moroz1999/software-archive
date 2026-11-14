import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {CreditedSoftware} from './credited-software';

/** The query of the software an owner is credited on, for the blocks inside a CreditedSoftwareProvider. */
export const [CreditedSoftwareContext, useCreditedSoftware] = createRequiredContext<UseQueryResult<readonly CreditedSoftware[]>>('CreditedSoftware');
