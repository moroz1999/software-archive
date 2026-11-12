import {useTranslation} from 'react-i18next';
import {AuthorForm, type AuthorId, AUTHORS_PATH} from '@/entities/authors';
import {CreateAuthorProvider} from '@/features/author/create';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';
import {AuthorLabels, AuthorLocations} from './AuthorFields';

export interface AuthorCreatorProps {
    /** Called with the new author's id once they are added. */
    readonly onCreated: (id: AuthorId) => void;
}

/** The page of adding an author: their real name, labels, locations and notes; Cancel returns to the catalog. */
export function AuthorCreator({onCreated}: AuthorCreatorProps) {
    const {t} = useTranslation();
    return (
        <PageLayout title={t('actions.addAuthor')}>
            <CreateAuthorProvider onCreated={onCreated}>
                <AuthorForm
                    submitLabel={t('actions.addAuthor')}
                    labels={<AuthorLabels/>}
                    locations={<AuthorLocations/>}
                    actions={<Button href={AUTHORS_PATH} color="secondary">{t('actions.cancel')}</Button>}
                />
            </CreateAuthorProvider>
        </PageLayout>
    );
}
