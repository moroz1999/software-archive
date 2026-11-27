import {api, apiError} from '@/shared/api';
import type {FullSearchPage} from '../model/found';
import type {FullSearchFilter} from '../model/full-search-filter';

/** A page of the full search: `GET /api/search`, of the kinds chosen and of the platform when one is chosen. */
export async function fetchFullSearch(filter: FullSearchFilter, platform: string | null, signal: AbortSignal): Promise<FullSearchPage> {
    const {data, error, response} = await api.GET('/api/search', {
        params: {query: {q: filter.keyword ?? '', types: filter.types.length > 0 ? filter.types : null, page: filter.page, platform}},
        signal,
    });
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}
