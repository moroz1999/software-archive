import {deleteResource, getJson, mapResponse, postJson, putJson, readArray, readNullableString, readObject, readString} from '@/shared/api';
import {type SoftwareRelease, toReleaseId, toReleaseType} from '../model/release';
import {
    type Hardware,
    type SoftwareDetails,
    type SoftwareId,
    toHardwareGroup,
    toReleaseDate,
    toSoftwarePlatformCode,
    toSoftwareId,
    toSoftwareTitle,
} from '../model/software';
import type {SoftwareDraft} from '../model/software-form';

function softwarePath(id: SoftwareId): string {
    return `/api/software/${id}`;
}

function toBody(draft: SoftwareDraft): unknown {
    return {title: draft.title, description: draft.description, releaseDate: draft.releaseDate, platform: draft.platform, hardware: draft.hardware};
}

/** The hardware of a release or software, or of a platform's reference book. */
export function parseHardwareList(raw: unknown): readonly Hardware[] {
    return readArray(raw, 'hardware').map((item) => {
        const hardware = readObject(item, 'hardware item');
        return {code: readString(hardware, 'code'), name: readString(hardware, 'name'), group: toHardwareGroup(readString(hardware, 'group'))};
    });
}

/** One software with its details: `GET /api/software/{id}`. */
export async function fetchSoftware(id: SoftwareId, signal: AbortSignal): Promise<SoftwareDetails> {
    const json = await getJson(softwarePath(id), signal);
    return mapResponse(() => {
        const dto = readObject(json, 'software');
        const releaseDate = readNullableString(dto, 'releaseDate');
        const platform = readObject(dto['platform'], 'platform');
        return {
            id: toSoftwareId(readString(dto, 'id')),
            title: toSoftwareTitle(readString(dto, 'title')),
            description: readNullableString(dto, 'description'),
            releaseDate: releaseDate === null ? null : toReleaseDate(releaseDate),
            platform: {code: toSoftwarePlatformCode(readString(platform, 'code')), name: readString(platform, 'name')},
            hardware: parseHardwareList(dto['hardware']),
        };
    });
}

/** The releases of one software, each with the hardware it shows: `GET /api/software/{id}/releases`. */
export async function fetchSoftwareReleases(id: SoftwareId, signal: AbortSignal): Promise<readonly SoftwareRelease[]> {
    const json = await getJson(`${softwarePath(id)}/releases`, signal);
    return mapResponse(() => readArray(json, 'releases').map((item) => {
        const dto = readObject(item, 'release');
        const releaseDate = readNullableString(dto, 'releaseDate');
        return {
            id: toReleaseId(readString(dto, 'id')),
            title: readString(dto, 'title'),
            version: readNullableString(dto, 'version'),
            type: toReleaseType(readString(dto, 'type')),
            releaseDate: releaseDate === null ? null : toReleaseDate(releaseDate),
            description: readNullableString(dto, 'description'),
            hardware: parseHardwareList(dto['hardware']),
        };
    }));
}

/** Adds software to the archive and returns its id. */
export async function createSoftware(draft: SoftwareDraft): Promise<SoftwareId> {
    const json = await postJson('/api/software', toBody(draft));
    return mapResponse(() => toSoftwareId(readString(readObject(json, 'created software'), 'id')));
}

export async function updateSoftware(id: SoftwareId, draft: SoftwareDraft): Promise<void> {
    await putJson(softwarePath(id), toBody(draft));
}

export async function deleteSoftware(id: SoftwareId): Promise<void> {
    await deleteResource(softwarePath(id));
}
