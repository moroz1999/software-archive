import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {QuickSearchResult} from './quick-search';

export interface QuickSearchState {
    /** What the visitor typed. */
    readonly text: string;
    readonly type: (text: string) => void;
    /** The keyword looked for, once the visitor stopped typing; null while there is none. */
    readonly keyword: string | null;
    /** The matches of the keyword; pending and idle without one. */
    readonly result: UseQueryResult<QuickSearchResult>;
    /** The next keyword has been loading over the shown matches for a while. */
    readonly changing: boolean;
}

/** The typed keyword and its matches, for the blocks inside a QuickSearchProvider. */
export const [QuickSearchContext, useQuickSearch] = createRequiredContext<QuickSearchState>('QuickSearch');
