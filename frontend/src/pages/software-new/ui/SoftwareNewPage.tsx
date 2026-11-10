import {useNavigate} from 'react-router';
import {useTranslation} from 'react-i18next';
import {SOFTWARE_LIST_PATH, SoftwareForm} from '@/entities/software';
import {CreateSoftwareProvider} from '@/features/software-create';
import {PageLayout} from '@/shared/ui/page-layout';

export function SoftwareNewPage() {
    const navigate = useNavigate();
    const {t} = useTranslation();
    return (
        <PageLayout title={t('actions.addSoftware')}>
            <CreateSoftwareProvider onCreated={() => {
                void navigate(SOFTWARE_LIST_PATH);
            }}>
                <SoftwareForm submitLabel={t('actions.addSoftware')}/>
            </CreateSoftwareProvider>
        </PageLayout>
    );
}
