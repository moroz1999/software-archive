import {queryOptions} from '@tanstack/react-query';
import {SOFTWARE_QUERY_KEY, type SoftwareId} from '@/entities/software';
import {fetchMyVote} from './my-votes-api';
import {fetchSoftwareRating, fetchSoftwareVotes} from './vote-api';

/** The rating of one software and the reader's vote, a block at the top of its page. */
export function softwareRatingQuery(id: SoftwareId) {
    return queryOptions({
        queryKey: [...SOFTWARE_QUERY_KEY, 'rating', id],
        queryFn: ({signal}) => fetchSoftwareRating(id, signal),
    });
}

/** The votes of one software, a block lower on its page. */
export function softwareVotesQuery(id: SoftwareId) {
    return queryOptions({
        queryKey: [...SOFTWARE_QUERY_KEY, 'votes', id],
        queryFn: ({signal}) => fetchSoftwareVotes(id, signal),
    });
}

/** The stars the reader gave one software, for its card in a list. */
export function myVoteQuery(id: SoftwareId) {
    return queryOptions({
        queryKey: [...SOFTWARE_QUERY_KEY, 'my-vote', id],
        queryFn: () => fetchMyVote(id),
    });
}
