import {GroupCatalog, GroupCatalogProvider} from '@/features/group-catalog';
import {PageLayout} from '@/shared/ui/page-layout';

export function GroupsPage() {
    const {t} = useTranslation();
    return (
        <PageLayout title={t('pages.groups')}>
            <GroupCatalogProvider>
                <GroupCatalog/>
            </GroupCatalogProvider>
        </PageLayout>
    );
}
import {useTranslation} from 'react-i18next';
