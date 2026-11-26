import {useTranslation} from 'react-i18next';
import {LatestSoftware, LatestSoftwareProvider} from '@/features/software/latest';
import {SoftwareCardVote} from '@/features/software/vote';
import {PageLayout} from '@/shared/ui/page-layout';

export function HomePage() {
    const {t} = useTranslation();
    return (
        <PageLayout title={t('pages.recentlyAdded')}>
            <LatestSoftwareProvider>
                <LatestSoftware cardFooter={(software) => <SoftwareCardVote software={software}/>}/>
            </LatestSoftwareProvider>
        </PageLayout>
    );
}
