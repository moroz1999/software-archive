import {AuthorCatalog, AuthorCatalogProvider} from '@/features/author-catalog';
import {PageLayout} from '@/shared/ui/page-layout';

export function AuthorsPage() {
    const {t} = useTranslation();
    return (
        <PageLayout title={t('pages.authors')}>
            <AuthorCatalogProvider>
                <AuthorCatalog/>
            </AuthorCatalogProvider>
        </PageLayout>
    );
}
import {useTranslation} from 'react-i18next';
