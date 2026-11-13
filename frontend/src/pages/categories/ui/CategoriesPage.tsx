import {useTranslation} from 'react-i18next';
import {CATEGORY_NEW_PATH, CategoryTree, CategoryTreeProvider} from '@/entities/categories';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';
import {useAbility} from '@/entities/users';
import {ManagementTabs} from '@/features/user/management-navigation';

export function CategoriesPage() {
    const {t} = useTranslation();
    const keepsCategories = useAbility('keep-categories');
    return (
        <PageLayout title={t('pages.categories')} actions={keepsCategories && <Button href={CATEGORY_NEW_PATH}>{t('actions.addCategory')}</Button>}>
            <ManagementTabs/>
            <CategoryTreeProvider>
                <CategoryTree editable={keepsCategories}/>
            </CategoryTreeProvider>
        </PageLayout>
    );
}
