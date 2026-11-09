import {AuthorCatalog, AuthorCatalogProvider} from '@/features/author-catalog';
import {PageLayout} from '@/shared/ui/page-layout';

export function AuthorsPage() {
    return (
        <PageLayout title="Authors">
            <AuthorCatalogProvider>
                <AuthorCatalog/>
            </AuthorCatalogProvider>
        </PageLayout>
    );
}
