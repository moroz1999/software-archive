import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {SoftwareRating} from './rating';
import type {CastVote} from './use-cast-vote';

export interface SoftwareRatingState extends CastVote {
    readonly query: UseQueryResult<SoftwareRating>;
}

export const [SoftwareRatingContext, useSoftwareRating] = createRequiredContext<SoftwareRatingState>('SoftwareRating');
