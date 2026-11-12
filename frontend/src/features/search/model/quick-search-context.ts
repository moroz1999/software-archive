import type {UseQueryResult} from '@tanstack/react-query';
import {createRequiredContext} from '@/shared/lib/context';
import type {FoundKind} from './found';
import type {QuickSearchResult} from './quick-search';

/** A match of the window: the id of its option, what it is and where it leads. */
export interface QuickMatch {
    readonly id: string;
    readonly kind: FoundKind;
    readonly title: string;
    readonly href: string;
}

/** The matches of one kind shown in the window, with the count of all of them. */
export interface QuickKind {
    readonly kind: FoundKind;
    readonly total: number;
    readonly matches: readonly QuickMatch[];
}

export interface QuickSearchState {
    /** What the visitor typed. */
    readonly text: string;
    /** Takes the typed text; forgets the chosen match. */
    readonly type: (text: string) => void;
    /** The keyword looked for, once the visitor stopped typing; null while there is none. */
    readonly keyword: string | null;
    /** The matches of the keyword; pending and idle without one. */
    readonly result: UseQueryResult<QuickSearchResult>;
    /** The kinds with matches, in their order; empty before the first matches and when nothing is found. */
    readonly kinds: readonly QuickKind[];
    /** The match chosen by the arrow keys; null when none is. */
    readonly chosen: QuickMatch | null;
    /** Chooses the next match ({@code 1}) or the one before ({@code -1}), round the list. */
    readonly move: (step: 1 | -1) => void;
    /** The id of the list of the matches, which the field controls. */
    readonly listId: string;
    /** The next keyword has been loading over the shown matches for a while. */
    readonly changing: boolean;
}

/** The typed keyword and its matches, for the blocks inside a QuickSearchProvider. */
export const [QuickSearchContext, useQuickSearch] = createRequiredContext<QuickSearchState>('QuickSearch');
