import {type ReactNode, useMemo} from 'react';
import {useQuery} from '@tanstack/react-query';
import {useNavigate, useSearchParams} from 'react-router';
import {usePlatformSelection} from '@/entities/platforms';
import {useLoadingLock} from '@/shared/lib/loading';
import {fullSearchQuery} from '../api/full-search-queries';
import {FullSearchContext} from './full-search-context';
import {type FullSearchFilter, fullSearchHref, parseFullSearchFilter} from './full-search-filter';

/**
 * Holds the keyword, the kinds and the page of the full search, read from the address, and its query; takes over the
 * call its route preloaded. Nothing is asked without a keyword.
 */
export function FullSearchProvider({children}: {readonly children: ReactNode}) {
    const [search] = useSearchParams();
    const navigate = useNavigate();
    const filter = useMemo(() => parseFullSearchFilter(search), [search]);
    const {platform} = usePlatformSelection();
    const result = useQuery({...fullSearchQuery(filter, platform), enabled: filter.keyword !== null});
    const changing = useLoadingLock(result.isPlaceholderData);

    const state = useMemo(() => ({
        filter,
        result,
        changing,
        show: (next: FullSearchFilter) => {
            void navigate(fullSearchHref(next));
        },
    }), [filter, result, changing, navigate]);

    return <FullSearchContext value={state}>{children}</FullSearchContext>;
}
