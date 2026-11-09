import {type ReactNode, useMemo} from 'react';
import {useQuery} from '@tanstack/react-query';
import {useNavigate, useSearchParams} from 'react-router';
import {citiesQuery, countriesQuery} from '@/entities/locations';
import {authorCatalogQuery} from '../api/catalog-queries';
import {AuthorCatalogContext} from './author-catalog-context';
import {type AuthorCatalogFilter, catalogHref, parseCatalogFilter} from './catalog-filter';

/**
 * Holds the catalog's filter, read from the address, and its queries for the blocks inside; takes over the calls
 * its route preloaded. A filter is changed by going to its address, so it can be shared and the back button works.
 */
export function AuthorCatalogProvider({children}: {readonly children: ReactNode}) {
    const [search] = useSearchParams();
    const navigate = useNavigate();
    const filter = useMemo(() => parseCatalogFilter(search), [search]);
    const entries = useQuery(authorCatalogQuery(filter));
    const countries = useQuery(countriesQuery());
    const cities = useQuery(citiesQuery(filter.country));

    const state = useMemo(() => ({
        filter,
        entries,
        countries,
        cities,
        changing: entries.isPlaceholderData || entries.isRefetching,
        show: (next: AuthorCatalogFilter) => {
            void navigate(catalogHref(next));
        },
    }), [filter, entries, countries, cities, navigate]);

    return <AuthorCatalogContext value={state}>{children}</AuthorCatalogContext>;
}
