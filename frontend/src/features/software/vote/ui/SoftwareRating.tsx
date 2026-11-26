import {useTranslation} from 'react-i18next';
import {Skeleton} from '@/shared/ui/skeleton';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {useSoftwareRating} from '../model/software-rating-context';
import styles from './SoftwareRating.module.css';
import {VoteStars} from './VoteStars';

/** The rating of software on its page, voted on there (VoteStars). */
export function SoftwareRating() {
    const {t} = useTranslation();
    const {query, ...cast} = useSoftwareRating();
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    const label = t('rating.title');
    return (
        <Stack as="section" gap="xs" ariaLabel={label}>
            <Text variant="label" tone="muted">{label}</Text>
            {query.isPending ? <Skeleton className={styles.bone}/> : <VoteStars rating={query.data} {...cast}/>}
        </Stack>
    );
}
