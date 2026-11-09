import {GroupCatalog, GroupCatalogProvider} from '@/features/group-catalog';
import {PageLayout} from '@/shared/ui/page-layout';

export function GroupsPage() {
    return (
        <PageLayout title="Groups">
            <GroupCatalogProvider>
                <GroupCatalog/>
            </GroupCatalogProvider>
        </PageLayout>
    );
}
