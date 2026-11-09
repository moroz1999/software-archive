import {getJson, mapResponse, type Page, parsePage, readNullableString, readObject, readString} from '@/shared/api';
import {type Software, toReleaseDate, toSoftwareId, toSoftwareTitle} from '../model/software';

const PAGE_SIZE = 20;

function parseSoftware(item: unknown): Software {
    const dto = readObject(item, 'software');
    const releaseDate = readNullableString(dto, 'releaseDate');
    return {
        id: toSoftwareId(readString(dto, 'id')),
        title: toSoftwareTitle(readString(dto, 'title')),
        releaseDate: releaseDate === null ? null : toReleaseDate(releaseDate),
    };
}

/** One page of the software list, newest contract of `GET /api/software`. */
export async function fetchSoftwarePage(page: number, signal: AbortSignal): Promise<Page<Software>> {
    const json = await getJson(`/api/software?page=${String(page)}&size=${String(PAGE_SIZE)}`, signal);
    return mapResponse(() => parsePage(json, parseSoftware));
}
