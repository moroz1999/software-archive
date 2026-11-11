import {getJson, mapResponse, type Page, parsePage, readNullableString, readObject, readString} from '@/shared/api';
import {type Software, toReleaseDate, toSoftwareId, toSoftwareTitle} from '../model/software';

/** Cards of the latest software on the home page. */
const LATEST_SIZE = 12;

/** The platform part of a list address; nothing for every platform. */
function platformParam(platform: string | null): string {
    return platform === null ? '' : `&platform=${encodeURIComponent(platform)}`;
}

/** A card of a list of software, as the API gives it. */
export function parseSoftware(item: unknown): Software {
    const dto = readObject(item, 'software');
    const releaseDate = readNullableString(dto, 'releaseDate');
    return {
        id: toSoftwareId(readString(dto, 'id')),
        title: toSoftwareTitle(readString(dto, 'title')),
        releaseDate: releaseDate === null ? null : toReleaseDate(releaseDate),
    };
}

/** The most recently added software, newest first, of the platform when one is given: `GET /api/software/latest`. */
export async function fetchLatestSoftware(platform: string | null, signal: AbortSignal): Promise<Page<Software>> {
    const json = await getJson(`/api/software/latest?size=${String(LATEST_SIZE)}${platformParam(platform)}`, signal);
    return mapResponse(() => parsePage(json, parseSoftware));
}
