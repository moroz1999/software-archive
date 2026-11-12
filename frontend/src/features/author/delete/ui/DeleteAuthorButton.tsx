import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {ConfirmDialog} from '@/shared/ui/confirm-dialog';
import {type DeletedAuthor, useDeleteAuthor} from '../model/delete-author-context';
import {DeleteAuthorProvider} from '../model/DeleteAuthorProvider';

export interface DeleteAuthorButtonProps {
    readonly author: DeletedAuthor;
    readonly onDeleted: () => void;
}

/** Deletes an author from the archive after asking; their labels stay as labels of nobody. */
export function DeleteAuthorButton({author, onDeleted}: DeleteAuthorButtonProps) {
    return (
        <DeleteAuthorProvider author={author} onDeleted={onDeleted}>
            <DeleteWithConfirmation/>
        </DeleteAuthorProvider>
    );
}

function DeleteWithConfirmation() {
    const {t} = useTranslation();
    const {author, remove, deleting, deleted, error} = useDeleteAuthor();
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
                title={t('author.delete')}
                confirmLabel={t('actions.deleteForGood')}
                onConfirm={remove}
                onCancel={() => {
                    setAsking(false);
                }}
                busy={deleting}
                error={error}
            >
                {t('author.deleteQuestion', {name: author.name})}
            </ConfirmDialog>
        </>
    );
}
