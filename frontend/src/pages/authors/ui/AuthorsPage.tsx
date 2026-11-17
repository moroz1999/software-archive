import {AUTHOR_NEW_PATH} from '@/entities/authors';
import {LABEL_NEW_PATH} from '@/entities/labels';
import {AuthorCatalog, AuthorCatalogProvider} from '@/features/author/catalog';
import {Button} from '@/shared/ui/button';
import {Inline} from '@/shared/ui/inline';
import {PageLayout} from '@/shared/ui/page-layout';

export function AuthorsPage() {
    const {t} = useTranslation();
    const addsEntries = useAbility('add-entries');
    return (
        <PageLayout title={t('pages.authors')} actions={addsEntries && (
            <Inline gap="sm">
                <Button href={LABEL_NEW_PATH} color="secondary">{t('actions.addLabel')}</Button>
                <Button href={AUTHOR_NEW_PATH}>{t('actions.addAuthor')}</Button>
            </Inline>
        )}>
            <AuthorCatalogProvider>
                <AuthorCatalog/>
            </AuthorCatalogProvider>
        </PageLayout>
    );
}
import {useTranslation} from 'react-i18next';
import {useAbility} from '@/entities/users';
