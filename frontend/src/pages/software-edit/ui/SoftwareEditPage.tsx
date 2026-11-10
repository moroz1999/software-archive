import {useNavigate, useParams} from 'react-router';
import {useTranslation} from 'react-i18next';
import {requireSoftwareId, SOFTWARE_LIST_PATH, softwarePath} from '@/entities/software';
import {PageLayout} from '@/shared/ui/page-layout';
import {SoftwareEditor} from '@/widgets/software-editor';

export function SoftwareEditPage() {
    const id = requireSoftwareId(useParams()['id']);
    const navigate = useNavigate();
    const {t} = useTranslation();
    return (
        <PageLayout title={t('actions.editSoftware')}>
            <SoftwareEditor
                id={id}
                onSaved={(softwareId) => {
                    void navigate(softwarePath(softwareId));
                }}
                onDeleted={() => {
                    void navigate(SOFTWARE_LIST_PATH);
                }}
            />
        </PageLayout>
    );
}
