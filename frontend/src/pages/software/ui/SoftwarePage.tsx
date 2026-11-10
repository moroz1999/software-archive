import {SOFTWARE_NEW_PATH} from '@/entities/software';
import {SoftwareList, SoftwareListProvider} from '@/features/software-list';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';

export function SoftwarePage() {
    const {t} = useTranslation();
    return (
        <PageLayout title={t('pages.software')} actions={<Button href={SOFTWARE_NEW_PATH}>{t('actions.addSoftware')}</Button>}>
            <SoftwareListProvider>
                <SoftwareList/>
            </SoftwareListProvider>
        </PageLayout>
    );
}
import {useTranslation} from 'react-i18next';
