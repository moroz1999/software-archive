import {useId} from 'react';
import {useTranslation} from 'react-i18next';
import {useSkeleton} from '@/shared/lib/loading';
import {dayText} from '@/shared/lib/i18n';
import {Inline} from '@/shared/ui/inline';
import {RatingStars} from '@/shared/ui/rating-stars';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Heading, Text} from '@/shared/ui/typography';
import {useSoftwareVotes} from '../model/software-votes-context';
import styles from './SoftwareVotes.module.css';

export interface SoftwareVotesProps {
    /** Tells the provider when the block comes into view. */
    readonly viewportRef?: (element: Element | null) => (() => void) | undefined;
}

/** Who voted on software and how, newest first. */
export function SoftwareVotes({viewportRef}: SoftwareVotesProps) {
    const {t, i18n} = useTranslation();
    const query = useSoftwareVotes();
    const skeleton = useSkeleton(query);
    const headingId = useId();
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    return (
        <section ref={viewportRef} aria-labelledby={headingId}>
            <Stack gap="md">
                <Heading level={2} variant="title" id={headingId}>{t('rating.votes')}</Heading>
                {skeleton || query.isPending ? (
                    <div data-testid="software-votes-skeleton">
                        <Stack gap="sm">
                            <Skeleton className={styles.bone}/>
                            <Skeleton className={styles.bone}/>
                        </Stack>
                    </div>
                ) : query.data.length === 0 ? (
                    <Text tone="muted">{t('rating.nobodyVoted')}</Text>
                ) : (
                    <Stack as="ul" gap="sm">
                        {query.data.map((vote) => (
                            <li key={`${vote.voter}-${vote.castAt}`}>
                                <Inline gap="md" align="center" wrap>
                                    <Text as="span">{vote.voter}</Text>
                                    <RatingStars value={vote.value} label={t('rating.stars', {stars: vote.value})} size="sm"/>
                                    <Text as="span" variant="bodySm" tone="muted">{dayText(vote.castAt, i18n.language)}</Text>
                                </Inline>
                            </li>
                        ))}
                    </Stack>
                )}
            </Stack>
        </section>
    );
}
