import {useTranslation} from 'react-i18next';
import {CATEGORIES_PATH, CategoryForm, type CategoryId, CategoryTreeProvider} from '@/entities/categories';
import {CreateCategoryProvider} from '@/features/category-create';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';

export interface CategoryCreatorProps {
    /** Called with the new category's id once it is added. */
    readonly onCreated: (id: CategoryId) => void;
}

/** The page of adding a category: its title and its parent in the tree; Cancel returns to the categories. */
export function CategoryCreator({onCreated}: CategoryCreatorProps) {
    const {t} = useTranslation();
    return (
        <PageLayout title={t('actions.addCategory')}>
            <CategoryTreeProvider>
                <CreateCategoryProvider onCreated={onCreated}>
                    <CategoryForm
                        submitLabel={t('actions.addCategory')}
                        actions={<Button href={CATEGORIES_PATH} color="secondary">{t('actions.cancel')}</Button>}
                    />
                </CreateCategoryProvider>
            </CategoryTreeProvider>
        </PageLayout>
    );
}
