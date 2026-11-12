import {useTranslation} from 'react-i18next';
import {
    CATEGORIES_PATH,
    CategoryDetailsProvider,
    CategoryForm,
    CategoryFormSkeleton,
    type CategoryId,
    CategoryTreeProvider,
    useCategoryDetails,
} from '@/entities/categories';
import {DeleteCategoryButton} from '@/features/category/delete';
import {EditCategoryProvider} from '@/features/category/edit';
import {useSkeleton} from '@/shared/lib/loading';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';

export interface CategoryEditorProps {
    readonly id: CategoryId;
    /** Called once the category is saved. */
    readonly onSaved: () => void;
    /** Called once the category is deleted. */
    readonly onDeleted: () => void;
}

/** The page of editing one category: Delete next to the title, the form with Cancel and Save under it. */
export function CategoryEditor(props: CategoryEditorProps) {
    return (
        <CategoryTreeProvider>
            <CategoryDetailsProvider key={props.id} id={props.id}>
                <LoadedEditor {...props}/>
            </CategoryDetailsProvider>
        </CategoryTreeProvider>
    );
}

function LoadedEditor({onSaved, onDeleted}: CategoryEditorProps) {
    const {t} = useTranslation();
    const query = useCategoryDetails();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return (
            <PageLayout title={t('actions.editCategory')}>
                <div data-testid="category-editor-skeleton"><CategoryFormSkeleton/></div>
            </PageLayout>
        );
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    return (
        <PageLayout title={t('actions.editCategory')} actions={<DeleteCategoryButton category={query.data} onDeleted={onDeleted}/>}>
            <EditCategoryProvider category={query.data} onSaved={onSaved}>
                <CategoryForm
                    submitLabel={t('actions.save')}
                    actions={<Button href={CATEGORIES_PATH} color="secondary">{t('actions.cancel')}</Button>}
                />
            </EditCategoryProvider>
        </PageLayout>
    );
}
