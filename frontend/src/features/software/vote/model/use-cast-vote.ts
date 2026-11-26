import {useMutation, useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import type {SoftwareId} from '@/entities/software';
import {businessErrorMessage, businessErrorOf} from '@/shared/api';
import {castVote} from '../api/vote-api';
import {myVoteQuery, softwareRatingQuery, softwareVotesQuery} from '../api/vote-queries';
import {voteMessages} from '../config/messages';
import type {SoftwareRating} from './rating';

export interface CastVote {
    /** Gives the stars; 0 withdraws the reader's vote. */
    readonly vote: (stars: number) => void;
    readonly voting: boolean;
    /** Why the archive refused the vote, for the reader. */
    readonly error: string | undefined;
}

/**
 * The reader's vote on one software, given and withdrawn: the answer is the new rating, kept for its page and its cards,
 * and the votes of the software are read again.
 */
export function useCastVote(id: SoftwareId, onAnswer?: (rating: SoftwareRating) => void): CastVote {
    const {t} = useTranslation();
    const queryClient = useQueryClient();
    const {mutate, isPending, error} = useMutation({
        mutationFn: (stars: number) => castVote(id, stars),
        onSuccess: async (rating) => {
            queryClient.setQueryData(softwareRatingQuery(id).queryKey, rating);
            queryClient.setQueryData(myVoteQuery(id).queryKey, rating.myVote);
            onAnswer?.(rating);
            await queryClient.invalidateQueries({queryKey: softwareVotesQuery(id).queryKey});
        },
    });
    const businessError = businessErrorOf(error);
    return {
        vote: (stars) => {
            mutate(stars);
        },
        voting: isPending,
        error: businessError === undefined ? undefined : businessErrorMessage(businessError, voteMessages(t)),
    };
}
