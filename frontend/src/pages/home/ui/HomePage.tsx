import {LatestSoftware, LatestSoftwareProvider} from '@/features/software-latest';
import {PageLayout} from '@/shared/ui/page-layout';

export function HomePage() {
    return (
        <PageLayout title="Recently added">
            <LatestSoftwareProvider>
                <LatestSoftware/>
            </LatestSoftwareProvider>
        </PageLayout>
    );
}
