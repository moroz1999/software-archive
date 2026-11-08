import {SOFTWARE_NEW_PATH} from '../../../entities/software';
import {SoftwareList} from '../../../features/software-list';
import {Button, PageLayout} from '../../../shared/ui';

export function SoftwarePage() {
    return (
        <PageLayout title="Software" actions={<Button href={SOFTWARE_NEW_PATH}>Add software</Button>}>
            <SoftwareList/>
        </PageLayout>
    );
}
