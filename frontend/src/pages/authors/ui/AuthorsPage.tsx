import {AUTHOR_NEW_PATH} from '@/entities/authors';
import {AuthorCatalog, AuthorCatalogProvider} from '@/features/author/catalog';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';

export function AuthorsPage() {
    const {t} = useTranslation();
    return (
        <PageLayout title={t('pages.authors')} actions={<Button href={AUTHOR_NEW_PATH}>{t('actions.addAuthor')}</Button>}>
            <AuthorCatalogProvider>
                <AuthorCatalog/>
            </AuthorCatalogProvider>
        </PageLayout>
    );
}
import {useTranslation} from 'react-i18next';
