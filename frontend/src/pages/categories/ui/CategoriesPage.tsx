import {useTranslation} from 'react-i18next';
import {CATEGORY_NEW_PATH, CategoryTree, CategoryTreeProvider} from '@/entities/categories';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';

export function CategoriesPage() {
    const {t} = useTranslation();
    return (
        <PageLayout title={t('pages.categories')} actions={<Button href={CATEGORY_NEW_PATH}>{t('actions.addCategory')}</Button>}>
            <CategoryTreeProvider>
                <CategoryTree/>
            </CategoryTreeProvider>
        </PageLayout>
    );
}
