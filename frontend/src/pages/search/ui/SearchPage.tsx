import {useTranslation} from 'react-i18next';
import {FullSearch, FullSearchProvider, useFullSearch} from '@/features/search';
import {PageLayout} from '@/shared/ui/page-layout';

export function SearchPage() {
    return (
        <FullSearchProvider>
            <SearchLayout/>
        </FullSearchProvider>
    );
}

/** Titled with the keyword, and the page after the first. */
function SearchLayout() {
    const {t} = useTranslation();
    const {filter} = useFullSearch();
    let title = t('pages.search');
    if (filter.keyword !== null) {
        title = filter.page > 0
            ? t('search.resultsPage', {keyword: filter.keyword, page: filter.page + 1})
            : t('search.results', {keyword: filter.keyword});
    }
    return (
        <PageLayout title={title}>
            <FullSearch/>
        </PageLayout>
    );
}
