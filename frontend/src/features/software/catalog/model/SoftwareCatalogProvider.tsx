import {type ReactNode, useMemo} from 'react';
import {useQuery} from '@tanstack/react-query';
import {useNavigate, useParams, useSearchParams} from 'react-router';
import {categoryTreeQuery} from '@/entities/categories';
import {citiesQuery, countriesQuery} from '@/entities/locations';
import {platformsQuery, usePlatformSelection} from '@/entities/platforms';
import {useLoadingLock} from '@/shared/lib/loading';
import {softwareCatalogQuery} from '../api/catalog-queries';
import {catalogHref, parseCatalogFilter, type SoftwareCatalogFilter} from './catalog-filter';
import {SoftwareCatalogContext} from './software-catalog-context';

/**
 * Holds the catalog's filter, read from the address, and its queries for the blocks inside; takes over the calls its
 * route preloaded. A filter is changed by going to its address, so it can be shared and the back button works.
 */
export function SoftwareCatalogProvider({children}: {readonly children: ReactNode}) {
    const {categoryId} = useParams();
    const [search] = useSearchParams();
    const navigate = useNavigate();
    const filter = useMemo(() => parseCatalogFilter(categoryId, search), [categoryId, search]);
    const {platform} = usePlatformSelection();
    const catalog = useQuery(softwareCatalogQuery(filter, platform));
    const categories = useQuery(categoryTreeQuery());
    const countries = useQuery(countriesQuery());
    const cities = useQuery(citiesQuery(filter.country));
    const platforms = useQuery(platformsQuery());

    const changing = useLoadingLock(catalog.isPlaceholderData || catalog.isRefetching);
    const state = useMemo(() => ({
        filter,
        catalog,
        categories,
        countries,
        cities,
        platforms,
        changing,
        show: (next: SoftwareCatalogFilter) => {
            void navigate(catalogHref(next));
        },
    }), [filter, changing, catalog, categories, countries, cities, platforms, navigate]);

    return <SoftwareCatalogContext value={state}>{children}</SoftwareCatalogContext>;
}
