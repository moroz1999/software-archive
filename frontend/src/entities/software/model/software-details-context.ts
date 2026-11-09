import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {SoftwareDetails} from './software';

/** The details query of the software, for the blocks inside a SoftwareDetailsProvider. */
export const [SoftwareDetailsContext, useSoftwareDetails] = createRequiredContext<UseQueryResult<SoftwareDetails>>('SoftwareDetails');
