import type {components} from '@/shared/api';

/** The sections of the quick search, each with its first matches and the count of all of them (docs/domain/search.md#quick-search). */
export type QuickSearchResult = components['schemas']['QuickSearchDto'];
