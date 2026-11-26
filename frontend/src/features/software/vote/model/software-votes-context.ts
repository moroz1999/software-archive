import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {SoftwareVote} from './rating';

export const [SoftwareVotesContext, useSoftwareVotes] = createRequiredContext<UseQueryResult<readonly SoftwareVote[]>>('SoftwareVotes');
