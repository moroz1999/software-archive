import {SoftwareCard, SoftwareCardSkeleton, softwarePath} from '@/entities/software';
import {CardGrid} from '@/shared/ui/card-grid';
import {LoadingState} from '@/shared/ui/loading-state';
import {Text} from '@/shared/ui/typography';
import {useSoftwareList} from '../model/software-list-context';

const SKELETON_CARDS = [1, 2, 3, 4, 5, 6, 7, 8];

/** The first page of the archive's software as cards, inside a SoftwareListProvider. */
export function SoftwareList() {
    const query = useSoftwareList();

    if (query.isPending) {
        return (
            <div data-testid="software-list-skeleton">
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
        return <Text tone="muted">No software in the archive yet.</Text>;
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
