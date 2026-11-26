import {useTranslation} from 'react-i18next';
import {LatestSoftware, LatestSoftwareProvider} from '@/features/software/latest';
import {SoftwareCard} from '@/features/software/vote';
import {PageLayout} from '@/shared/ui/page-layout';

export function HomePage() {
    const {t} = useTranslation();
    return (
        <PageLayout title={t('pages.recentlyAdded')}>
            <LatestSoftwareProvider>
                <LatestSoftware Card={SoftwareCard}/>
            </LatestSoftwareProvider>
        </PageLayout>
    );
}
