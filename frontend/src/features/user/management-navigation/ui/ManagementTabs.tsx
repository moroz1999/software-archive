import {useTranslation} from 'react-i18next';
import {CATEGORIES_PATH} from '@/entities/categories';
import {HARDWARE_PATH} from '@/entities/hardware';
import {GEOGRAPHY_PATH} from '@/entities/locations';
import {MANAGEMENT_USERS_PATH} from '@/entities/users';
import {MANAGEMENT_IMPORTS_PATH} from '@/shared/config';
import {Tab, Tabs} from '@/shared/ui/tabs';

/** The sibling sections of archive management. */
export function ManagementTabs() {
    const {t} = useTranslation();
    return (
        <Tabs label={t('navigation.management')}>
            <Tab href={CATEGORIES_PATH}>{t('navigation.categories')}</Tab>
            <Tab href={GEOGRAPHY_PATH}>{t('navigation.geography')}</Tab>
            <Tab href={HARDWARE_PATH}>{t('navigation.hardware')}</Tab>
            <Tab href={MANAGEMENT_USERS_PATH}>{t('navigation.users')}</Tab>
            <Tab href={MANAGEMENT_IMPORTS_PATH}>{t('navigation.imports')}</Tab>
        </Tabs>
    );
}
