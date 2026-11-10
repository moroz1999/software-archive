import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {ReleaseDetails} from './release';

/** The query of a release as it states itself, for the blocks inside a ReleaseDetailsProvider. */
export const [ReleaseDetailsContext, useReleaseDetails] = createRequiredContext<UseQueryResult<ReleaseDetails>>('ReleaseDetails');
