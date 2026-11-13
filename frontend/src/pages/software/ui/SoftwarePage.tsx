import {useTranslation} from 'react-i18next';
import {SOFTWARE_NEW_PATH} from '@/entities/software';
import {SoftwareCatalog, SoftwareCatalogProvider} from '@/features/software/catalog';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';
import {useAbility} from '@/entities/users';

export function SoftwarePage() {
    const {t} = useTranslation();
    const addsEntries = useAbility('add-entries');
    return (
        <PageLayout title={t('pages.software')} actions={addsEntries && <Button href={SOFTWARE_NEW_PATH}>{t('actions.addSoftware')}</Button>}>
            <SoftwareCatalogProvider>
                <SoftwareCatalog/>
            </SoftwareCatalogProvider>
        </PageLayout>
    );
}
