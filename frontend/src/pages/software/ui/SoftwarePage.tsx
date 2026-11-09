import {SOFTWARE_NEW_PATH} from '@/entities/software';
import {SoftwareList, SoftwareListProvider} from '@/features/software-list';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';

export function SoftwarePage() {
    return (
        <PageLayout title="Software" actions={<Button href={SOFTWARE_NEW_PATH}>Add software</Button>}>
            <SoftwareListProvider>
                <SoftwareList/>
            </SoftwareListProvider>
        </PageLayout>
    );
}
