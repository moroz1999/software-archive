import type {Software} from '@/entities/software';
import type {FoundAuthor, FoundGroup} from './found';

/** The first matches of a section and the count of all of them. */
export interface QuickSection<T> {
    readonly items: readonly T[];
    readonly total: number;
}

/** The sections of the quick search (docs/domain/search.md#quick-search). */
export interface QuickSearchResult {
    readonly software: QuickSection<Software>;
    readonly authors: QuickSection<FoundAuthor>;
    readonly groups: QuickSection<FoundGroup>;
}
