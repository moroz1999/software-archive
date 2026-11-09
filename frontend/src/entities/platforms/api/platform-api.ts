import {getJson, mapResponse, readArray, readObject, readString} from '@/shared/api';
import {type Platform, toPlatformCode} from '../model/platform';

/** The platforms of the archive, by name: `GET /api/platforms`. */
export async function fetchPlatforms(signal: AbortSignal): Promise<readonly Platform[]> {
    const json = await getJson('/api/platforms', signal);
    return mapResponse(() => readArray(json, 'platforms').map((item) => {
        const dto = readObject(item, 'platform');
        return {code: toPlatformCode(readString(dto, 'code')), name: readString(dto, 'name')};
    }));
}
