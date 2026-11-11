import {useTranslation} from 'react-i18next';
import {AuthorDetailsProvider, AuthorForm, AuthorFormSkeleton, type AuthorId, authorPath, useAuthorDetails} from '@/entities/authors';
import {DeleteAuthorButton} from '@/features/author-delete';
import {EditAuthorProvider} from '@/features/author-edit';
import {useSkeleton} from '@/shared/lib/skeleton';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';
import {AuthorLabels, AuthorLocations} from './AuthorFields';

export interface AuthorEditorProps {
    readonly id: AuthorId;
    /** Called once the author is saved. */
    readonly onSaved: () => void;
    /** Called once the author is deleted. */
    readonly onDeleted: () => void;
}

/** The page of editing one author: Delete next to the title, the form with Cancel and Save under it. */
export function AuthorEditor(props: AuthorEditorProps) {
    return (
        <AuthorDetailsProvider key={props.id} id={props.id}>
            <LoadedEditor {...props}/>
        </AuthorDetailsProvider>
    );
}

function LoadedEditor({id, onSaved, onDeleted}: AuthorEditorProps) {
    const {t} = useTranslation();
    const query = useAuthorDetails();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return (
            <PageLayout title={t('actions.editAuthor')}>
                <div data-testid="author-editor-skeleton"><AuthorFormSkeleton/></div>
            </PageLayout>
        );
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    return (
        <PageLayout title={t('actions.editAuthor')} actions={<DeleteAuthorButton author={query.data} onDeleted={onDeleted}/>}>
            <EditAuthorProvider author={query.data} onSaved={onSaved}>
                <AuthorForm
                    submitLabel={t('actions.save')}
                    labels={<AuthorLabels/>}
                    locations={<AuthorLocations/>}
                    actions={<Button href={authorPath(id)} color="secondary">{t('actions.cancel')}</Button>}
                />
            </EditAuthorProvider>
        </PageLayout>
    );
}
