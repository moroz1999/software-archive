import {deleteResource, getJson, mapResponse, postJson, putJson, readNullableString, readObject, readString} from '@/shared/api';
import {type ReleaseDetails, type ReleaseId, toReleaseId, toReleaseType} from '../model/release';
import type {ReleaseDraft} from '../model/release-form';
import {type Hardware, type SoftwareId, type SoftwarePlatformCode, toReleaseDate} from '../model/software';
import {parseHardwareList} from './software-api';

function releasesPath(softwareId: SoftwareId): string {
    return `/api/software/${softwareId}/releases`;
}

function releasePath(softwareId: SoftwareId, releaseId: ReleaseId): string {
    return `${releasesPath(softwareId)}/${releaseId}`;
}

function toBody(draft: ReleaseDraft): unknown {
    return {
        title: draft.title,
        version: draft.version,
        type: draft.type,
        releaseDate: draft.releaseDate,
        description: draft.description,
        hardware: draft.hardware,
    };
}

/** A release as it states itself: `GET /api/software/{id}/releases/{releaseId}`. */
export async function fetchSoftwareRelease(softwareId: SoftwareId, releaseId: ReleaseId, signal: AbortSignal): Promise<ReleaseDetails> {
    const json = await getJson(releasePath(softwareId, releaseId), signal);
    return mapResponse(() => {
        const dto = readObject(json, 'release');
        const releaseDate = readNullableString(dto, 'releaseDate');
        return {
            id: toReleaseId(readString(dto, 'id')),
            title: readNullableString(dto, 'title'),
            version: readNullableString(dto, 'version'),
            type: toReleaseType(readString(dto, 'type')),
            releaseDate: releaseDate === null ? null : toReleaseDate(releaseDate),
            description: readNullableString(dto, 'description'),
            hardware: parseHardwareList(dto['hardware']),
        };
    });
}

/** Adds a release to its software and returns its id. */
export async function createRelease(softwareId: SoftwareId, draft: ReleaseDraft): Promise<ReleaseId> {
    const json = await postJson(releasesPath(softwareId), toBody(draft));
    return mapResponse(() => toReleaseId(readString(readObject(json, 'created release'), 'id')));
}

export async function updateRelease(softwareId: SoftwareId, releaseId: ReleaseId, draft: ReleaseDraft): Promise<void> {
    await putJson(releasePath(softwareId, releaseId), toBody(draft));
}

export async function deleteRelease(softwareId: SoftwareId, releaseId: ReleaseId): Promise<void> {
    await deleteResource(releasePath(softwareId, releaseId));
}

/** The hardware of a platform, to choose from: `GET /api/hardware?platform={code}`. */
export async function fetchPlatformHardware(platform: SoftwarePlatformCode, signal: AbortSignal): Promise<readonly Hardware[]> {
    const json = await getJson(`/api/hardware?${new URLSearchParams({platform}).toString()}`, signal);
    return mapResponse(() => parseHardwareList(json));
}
