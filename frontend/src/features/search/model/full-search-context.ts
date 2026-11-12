import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {FullSearchPage} from './found';
import type {FullSearchFilter} from './full-search-filter';

export interface FullSearchState {
    /** What the address asks the search to show. */
    readonly filter: FullSearchFilter;
    /** The page of the keyword; idle without one. */
    readonly result: UseQueryResult<FullSearchPage>;
    /** Another page or choice of kinds has been loading over the shown one for a while: the controls are locked. */
    readonly changing: boolean;
    /** Goes to the address of another page or choice of kinds. */
    readonly show: (filter: FullSearchFilter) => void;
}

/** The keyword, the kinds, the page and the matches of the full search, for the blocks inside a FullSearchProvider. */
export const [FullSearchContext, useFullSearch] = createRequiredContext<FullSearchState>('FullSearch');
