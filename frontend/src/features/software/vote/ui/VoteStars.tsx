import {useTranslation} from 'react-i18next';
import {useCurrentUser} from '@/entities/users';
import {decimalText} from '@/shared/lib/i18n';
import {Button} from '@/shared/ui/button';
import {ErrorState} from '@/shared/ui/error-state';
import {CloseIcon} from '@/shared/ui/icon';
import {Inline} from '@/shared/ui/inline';
import {RatingStars} from '@/shared/ui/rating-stars';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import type {CastVote} from '../model/use-cast-vote';
import type {SoftwareRating} from '../model/rating';

export interface VoteStarsProps extends CastVote {
    readonly rating: SoftwareRating;
    readonly size?: 'sm' | 'md';
}

/**
 * The rating as stars, the number of votes behind it in their tooltip; a signed-in reader gives stars, sees their own
 * vote as a number beside them and withdraws it with a cross. The same on the page of software and on its cards.
 */
export function VoteStars({rating: {rating, votesAmount, myVote}, vote, voting, error, size = 'md'}: VoteStarsProps) {
    const {t, i18n} = useTranslation();
    const signedIn = useCurrentUser().user !== null;
    return (
        <Stack gap="xs">
            <Inline gap="sm" align="center">
                <RatingStars
                    value={rating}
                    label={t('rating.label', {rating: decimalText(rating, i18n.language)})}
                    title={votesAmount > 0 ? t('rating.votesAmount', {count: votesAmount}) : t('rating.none')}
                    {...(signedIn ? {onRate: vote, starLabel: (stars: number) => t('rating.give', {count: stars})} : {})}
                    chosen={signedIn ? myVote : null}
                    disabled={voting}
                    size={size}
                />
                {signedIn && myVote !== null && (
                    <>
                        <Text as="span" variant="bodySm">{myVote}</Text>
                        <Button
                            size="xs"
                            color="transparent"
                            square
                            ariaLabel={t('rating.withdraw')}
                            disabled={voting}
                            onClick={() => {
                                vote(0);
                            }}
                        >
                            <CloseIcon/>
                        </Button>
                    </>
                )}
            </Inline>
            {error !== undefined && <ErrorState message={error}/>}
        </Stack>
    );
}
