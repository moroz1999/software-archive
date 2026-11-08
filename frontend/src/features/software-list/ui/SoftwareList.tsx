import {SoftwareCard, SoftwareCardSkeleton, softwareEditPath} from '../../../entities/software';
import {businessErrorMessage, useRequest} from '../../../shared/api';
import {CardGrid, ErrorState, LoadingState, Text} from '../../../shared/ui';
import {softwareListRequest} from '../api/software-list-request';
import {softwareListMessages} from '../config/messages';

const SKELETON_CARDS = [1, 2, 3, 4, 5, 6, 7, 8];

/** The first page of the archive's software as cards. */
export function SoftwareList() {
    const {state, reload} = useRequest(softwareListRequest);

    switch (state.status) {
        case 'loading':
            return (
                <div data-testid="software-list-skeleton">
                    <CardGrid>
                        {SKELETON_CARDS.map((card) => <li key={card}><SoftwareCardSkeleton/></li>)}
                    </CardGrid>
                </div>
            );
        case 'system-error':
            // Shown by the system error dialog (docs/error-handling.md).
            return null;
        case 'business-error':
            return <ErrorState message={businessErrorMessage(state.error, softwareListMessages)} onRetry={reload}/>;
        case 'success':
            if (state.data.items.length === 0) {
                return <Text tone="muted">No software in the archive yet.</Text>;
            }
            return (
                <LoadingState loading={state.reloading}>
                    <CardGrid>
                        {state.data.items.map((software) => (
                            <li key={software.id}>
                                <SoftwareCard software={software} href={softwareEditPath(software.id)}/>
                            </li>
                        ))}
                    </CardGrid>
                </LoadingState>
            );
    }
}
