import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {GroupDetails} from './group';

/** The details query of the group, for the blocks inside a GroupDetailsProvider. */
export const [GroupDetailsContext, useGroupDetails] = createRequiredContext<UseQueryResult<GroupDetails>>('GroupDetails');
