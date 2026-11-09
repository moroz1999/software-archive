import {type ReactNode, useMemo} from 'react';
import {useQuery} from '@tanstack/react-query';
import {useNavigate, useSearchParams} from 'react-router';
import {citiesQuery, countriesQuery} from '@/entities/locations';
import {groupCatalogQuery} from '../api/catalog-queries';
import {type GroupCatalogFilter, groupCatalogHref, parseGroupCatalogFilter} from './catalog-filter';
import {GroupCatalogContext} from './group-catalog-context';

/**
 * Holds the catalog's filter, read from the address, and its queries for the blocks inside; takes over the calls
 * its route preloaded. A filter is changed by going to its address, so it can be shared and the back button works.
 */
export function GroupCatalogProvider({children}: {readonly children: ReactNode}) {
    const [search] = useSearchParams();
    const navigate = useNavigate();
    const filter = useMemo(() => parseGroupCatalogFilter(search), [search]);
    const entries = useQuery(groupCatalogQuery(filter));
    const countries = useQuery(countriesQuery());
    const cities = useQuery(citiesQuery(filter.country));

    const state = useMemo(() => ({
        filter,
        entries,
        countries,
        cities,
        changing: entries.isPlaceholderData || entries.isRefetching,
        show: (next: GroupCatalogFilter) => {
            void navigate(groupCatalogHref(next));
        },
    }), [filter, entries, countries, cities, navigate]);

    return <GroupCatalogContext value={state}>{children}</GroupCatalogContext>;
}
