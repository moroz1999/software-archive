import {deleteResource, getJson, mapResponse, postForm, postNoContent, putForm, readArray, readBoolean, readNullableString, readNumber, readObject, readString} from '@/shared/api';
import {type ReleaseDetails, type ReleaseId, toReleaseId, toReleaseType} from '../model/release';
import type {ReleaseArchive} from '../model/release-archive';
import {type ReleaseFile, toReleaseFileId} from '../model/release-file';
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

function toForm(draft: ReleaseDraft): FormData {
    const form = new FormData();
    form.append('release', new Blob([JSON.stringify(toBody(draft))], {type: 'application/json'}));
    if (draft.archive !== undefined) {
        form.append('archive', draft.archive);
    }
    if (draft.removeArchive === true) {
        form.append('removeArchive', 'true');
    }
    return form;
}

/** An archive in a release answer; missing means the release has no downloadable file. */
export function parseReleaseArchive(raw: unknown): ReleaseArchive | null {
    if (raw === undefined || raw === null) {
        return null;
    }
    const dto = readObject(raw, 'release archive');
    return {fileName: readString(dto, 'fileName'), size: readNumber(dto, 'size'), url: readString(dto, 'url')};
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
            archive: parseReleaseArchive(dto['archive']),
        };
    });
}

function parseReleaseFile(raw: unknown): ReleaseFile {
    const dto = readObject(raw, 'release file');
    return {
        id: toReleaseFileId(readString(dto, 'id')),
        name: readString(dto, 'name'),
        directory: readBoolean(dto, 'directory'),
        executable: readBoolean(dto, 'executable'),
        unreadable: readBoolean(dto, 'unreadable'),
        children: readArray(dto['children'], 'release file children').map(parseReleaseFile),
    };
}

/** The parsed tree rooted at a release's downloadable file; null while the file waits for parsing. */
export async function fetchReleaseFiles(softwareId: SoftwareId, releaseId: ReleaseId, signal: AbortSignal): Promise<ReleaseFile | null> {
    const json = await getJson(`${releasePath(softwareId, releaseId)}/files`, signal);
    return mapResponse(() => {
        const root = readObject(json, 'release files')['root'];
        return root === null ? null : parseReleaseFile(root);
    });
}

/** Builds the tree of a release file again; only whoever may change the release may ask. */
export async function reparseReleaseFiles(softwareId: SoftwareId, releaseId: ReleaseId): Promise<void> {
    await postNoContent(`${releasePath(softwareId, releaseId)}/files/reparse`);
}

/** Adds a release to its software and returns its id. */
export async function createRelease(softwareId: SoftwareId, draft: ReleaseDraft): Promise<ReleaseId> {
    const json = await postForm(releasesPath(softwareId), toForm(draft));
    return mapResponse(() => toReleaseId(readString(readObject(json, 'created release'), 'id')));
}

export async function updateRelease(softwareId: SoftwareId, releaseId: ReleaseId, draft: ReleaseDraft): Promise<void> {
    await putForm(releasePath(softwareId, releaseId), toForm(draft));
}

export async function deleteRelease(softwareId: SoftwareId, releaseId: ReleaseId): Promise<void> {
    await deleteResource(releasePath(softwareId, releaseId));
}

/** The hardware of a platform, to choose from: `GET /api/hardware?platform={code}`. */
export async function fetchPlatformHardware(platform: SoftwarePlatformCode, signal: AbortSignal): Promise<readonly Hardware[]> {
    const json = await getJson(`/api/hardware?${new URLSearchParams({platform}).toString()}`, signal);
    return mapResponse(() => parseHardwareList(json));
}
