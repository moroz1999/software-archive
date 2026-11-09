import {useNavigate} from 'react-router';
import {SOFTWARE_LIST_PATH, SoftwareForm} from '@/entities/software';
import {CreateSoftwareProvider} from '@/features/software-create';
import {PageLayout} from '@/shared/ui/page-layout';

export function SoftwareNewPage() {
    const navigate = useNavigate();
    return (
        <PageLayout title="Add software">
            <CreateSoftwareProvider onCreated={() => {
                void navigate(SOFTWARE_LIST_PATH);
            }}>
                <SoftwareForm submitLabel="Add software"/>
            </CreateSoftwareProvider>
        </PageLayout>
    );
}
