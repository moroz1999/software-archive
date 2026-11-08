import {useNavigate} from 'react-router';
import {SOFTWARE_LIST_PATH} from '../../../entities/software';
import {CreateSoftwareForm} from '../../../features/software-create';
import {PageLayout} from '../../../shared/ui';

export function SoftwareNewPage() {
    const navigate = useNavigate();
    return (
        <PageLayout title="Add software">
            <CreateSoftwareForm onCreated={() => {
                void navigate(SOFTWARE_LIST_PATH);
            }}/>
        </PageLayout>
    );
}
