import {useTranslation} from 'react-i18next';
import {CATEGORIES_PATH} from '@/entities/categories';
import {MANAGEMENT_USERS_PATH} from '@/entities/users';
import {Tab, Tabs} from '@/shared/ui/tabs';

/** The sibling sections of archive management. */
export function ManagementTabs() {
    const {t} = useTranslation();
    return (
        <Tabs label={t('navigation.management')}>
            <Tab href={CATEGORIES_PATH}>{t('navigation.categories')}</Tab>
            <Tab href={MANAGEMENT_USERS_PATH}>{t('navigation.users')}</Tab>
        </Tabs>
    );
}
