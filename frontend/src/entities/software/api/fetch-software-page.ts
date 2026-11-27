import {api, apiError, type components} from '@/shared/api';
import {type Software} from '../model/software';

/** The most recently added software, newest first, of the platform when one is given: `GET /api/software/latest`. */
export async function fetchLatestSoftware(platform: string | null, signal: AbortSignal): Promise<components['schemas']['PagedModelSoftwareListItemDto']> {
    const {data, error, response} = await api.GET('/api/software/latest', {params: {query: platform === null ? {} : {platform}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

export async function fetchCompilationPrograms(id: string, signal: AbortSignal): Promise<readonly Software[]> {
    const {data, error, response} = await api.GET('/api/software/{id}/compilation-items', {params: {path: {id}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}
