import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {ConfirmDialog} from '@/shared/ui/confirm-dialog';
import {type DeletedCategory, useDeleteCategory} from '../model/delete-category-context';
import {DeleteCategoryProvider} from '../model/DeleteCategoryProvider';

export interface DeleteCategoryButtonProps {
    readonly category: DeletedCategory;
    readonly onDeleted: () => void;
}

/** Deletes a category with its subcategories after asking; their software stays in the archive. */
export function DeleteCategoryButton({category, onDeleted}: DeleteCategoryButtonProps) {
    return (
        <DeleteCategoryProvider category={category} onDeleted={onDeleted}>
            <DeleteWithConfirmation/>
        </DeleteCategoryProvider>
    );
}

function DeleteWithConfirmation() {
    const {t} = useTranslation();
    const {category, remove, deleting, deleted, error} = useDeleteCategory();
    // Whether the confirmation is open is the dialog's own UI state, not business state.
    const [asking, setAsking] = useState(false);

    return (
        <>
            <Button color="danger" onClick={() => {
                setAsking(true);
            }}>
                {t('actions.delete')}
            </Button>
            <ConfirmDialog
                open={asking && !deleted}
                title={t('category.delete')}
                confirmLabel={t('actions.deleteForGood')}
                onConfirm={remove}
                onCancel={() => {
                    setAsking(false);
                }}
                busy={deleting}
                error={error}
            >
                {t('category.deleteQuestion', {title: category.title})}
            </ConfirmDialog>
        </>
    );
}
