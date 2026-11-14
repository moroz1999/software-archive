import {useTranslation} from 'react-i18next';
import {VektorImport} from '@/features/import/vektor';
import {ManagementTabs} from '@/features/user/management-navigation';
import {PageLayout} from '@/shared/ui/page-layout';

/** The imports from other archives, started by an administrator. */
export function ManagementImportsPage() {
    const {t} = useTranslation();
    return (
        <PageLayout title={t('pages.imports')}>
            <ManagementTabs/>
            <VektorImport/>
        </PageLayout>
    );
}
