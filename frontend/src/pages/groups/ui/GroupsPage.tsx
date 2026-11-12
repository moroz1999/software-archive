import {GROUP_NEW_PATH} from '@/entities/groups';
import {GroupCatalog, GroupCatalogProvider} from '@/features/group/catalog';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';

export function GroupsPage() {
    const {t} = useTranslation();
    return (
        <PageLayout title={t('pages.groups')} actions={<Button href={GROUP_NEW_PATH}>{t('actions.addGroup')}</Button>}>
            <GroupCatalogProvider>
                <GroupCatalog/>
            </GroupCatalogProvider>
        </PageLayout>
    );
}
import {useTranslation} from 'react-i18next';
