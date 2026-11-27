import {api, apiError} from '@/shared/api';
import type {QuickSearchResult} from '../model/quick-search';

/** The first matches of the keyword in every section: `GET /api/search/quick`, of the platform when one is chosen. */
export async function fetchQuickSearch(keyword: string, platform: string | null, signal: AbortSignal): Promise<QuickSearchResult> {
    const {data, error, response} = await api.GET('/api/search/quick', {params: {query: {q: keyword, platform}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}
