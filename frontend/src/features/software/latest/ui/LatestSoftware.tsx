import type {ReactNode} from 'react';
import {type Software, SoftwareCard, SoftwareCardSkeleton, softwarePath} from '@/entities/software';
import {CardGrid} from '@/shared/ui/card-grid';
import {LoadingState} from '@/shared/ui/loading-state';
import {Text} from '@/shared/ui/typography';
import {useLatestSoftware} from '../model/latest-software-context';
import {useSkeleton} from '@/shared/lib/loading';

const SKELETON_CARDS = [1, 2, 3, 4, 5, 6, 7, 8];

/** The latest software as cards, newest first, inside a LatestSoftwareProvider. */
export interface LatestSoftwareProps {
    /** What each card holds under it, outside its link, such as voting. */
    readonly cardFooter?: ((software: Software) => ReactNode) | undefined;
}

export function LatestSoftware({cardFooter}: LatestSoftwareProps = {}) {
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
                        <SoftwareCard software={software} href={softwarePath(software.id)} footer={cardFooter?.(software)}/>
                    </li>
                ))}
            </CardGrid>
        </LoadingState>
    );
}
import {useTranslation} from 'react-i18next';
