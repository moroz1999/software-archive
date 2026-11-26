import {useState} from 'react';
import {useQuery} from '@tanstack/react-query';
import type {CardRating, Software} from '@/entities/software';
import {useCurrentUser} from '@/entities/users';
import {myVoteQuery} from '../api/vote-queries';
import type {SoftwareRating} from '../model/rating';
import {useCastVote} from '../model/use-cast-vote';
import {VoteStars} from './VoteStars';

/** The rating a vote answered with, over the rating of the card it was cast on. */
interface Answer {
    readonly to: CardRating | undefined;
    readonly rating: SoftwareRating;
}

const UNRATED: CardRating = {rating: 0, votesAmount: 0};

export interface SoftwareCardVoteProps {
    readonly software: Software;
}

/**
 * The rating on a card of software in a list, voted on there as on its page.
 * The answer to a vote stands on the card until the list brings another rating.
 */
export function SoftwareCardVote({software}: SoftwareCardVoteProps) {
    const signedIn = useCurrentUser().user !== null;
    const mine = useQuery({...myVoteQuery(software.id), enabled: signedIn});
    const [answer, setAnswer] = useState<Answer | null>(null);
    const cast = useCastVote(software.id, (rating) => {
        setAnswer({to: software.rating, rating});
    });
    const rating = answer !== null && answer.to === software.rating
        ? answer.rating
        : {...(software.rating ?? UNRATED), myVote: mine.data ?? null};
    return <VoteStars rating={rating} {...cast}/>;
}
