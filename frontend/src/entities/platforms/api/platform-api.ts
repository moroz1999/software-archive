import {api, apiError} from '@/shared/api';
import {type Platform} from '../model/platform';

/** The platforms of the archive, by name: `GET /api/platforms`. */
export async function fetchPlatforms(signal: AbortSignal): Promise<readonly Platform[]> {
    const {data, error, response} = await api.GET('/api/platforms', {signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}
