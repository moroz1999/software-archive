import {useTranslation} from 'react-i18next';
import {HARDWARE_GROUP_NEW_PATH, HARDWARE_NEW_PATH, HardwareGroupsList, HardwareGroupsProvider} from '@/entities/hardware';
import {ManagementTabs} from '@/features/user/management-navigation';
import {Button} from '@/shared/ui/button';
import {Inline} from '@/shared/ui/inline';
import {PageLayout} from '@/shared/ui/page-layout';

export function ManagementHardwarePage() {
    const {t} = useTranslation();
    return (
        <PageLayout
            title={t('pages.hardware')}
            actions={(
                <Inline gap="sm">
                    <Button href={HARDWARE_NEW_PATH}>{t('actions.addHardware')}</Button>
                    <Button href={HARDWARE_GROUP_NEW_PATH} color="secondary">{t('actions.addGroup')}</Button>
                </Inline>
            )}
        >
            <ManagementTabs/>
            <HardwareGroupsProvider>
                <HardwareGroupsList/>
            </HardwareGroupsProvider>
        </PageLayout>
    );
}
