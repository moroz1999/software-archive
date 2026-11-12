import {SoftwareCard, SoftwareCardSkeleton, softwarePath} from '@/entities/software';
import {CardGrid} from '@/shared/ui/card-grid';
import {LoadingState} from '@/shared/ui/loading-state';
import {Text} from '@/shared/ui/typography';
import {useLatestSoftware} from '../model/latest-software-context';
import {useSkeleton} from '@/shared/lib/loading';

const SKELETON_CARDS = [1, 2, 3, 4, 5, 6, 7, 8];

/** The latest software as cards, newest first, inside a LatestSoftwareProvider. */
export function LatestSoftware() {
    const {t} = useTranslation();
    const query = useLatestSoftware();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return (
            <div data-testid="latest-software-skeleton">
                <CardGrid>
                    {SKELETON_CARDS.map((card) => <li key={card}><SoftwareCardSkeleton/></li>)}
                </CardGrid>
            </div>
        );
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    if (query.data.items.length === 0) {
        return <Text tone="muted">{t('empty.software')}</Text>;
    }
    return (
        <LoadingState loading={query.isRefetching}>
            <CardGrid>
                {query.data.items.map((software) => (
                    <li key={software.id}>
                        <SoftwareCard software={software} href={softwarePath(software.id)}/>
                    </li>
                ))}
            </CardGrid>
        </LoadingState>
    );
}
import {useTranslation} from 'react-i18next';
