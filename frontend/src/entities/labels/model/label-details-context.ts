import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {LabelDetails} from './label';

/** The details query of the label, for the blocks inside a LabelDetailsProvider. */
export const [LabelDetailsContext, useLabelDetails] = createRequiredContext<UseQueryResult<LabelDetails>>('LabelDetails');
