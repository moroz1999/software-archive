import {api, apiError, type components, multipartForm} from '@/shared/api';
import {type ReleaseDetails} from '../model/release';
import {type ReleaseFile} from '../model/release-file';
import type {ReleaseDraft} from '../model/release-form';
import {type Hardware} from '../model/software';

type ReleaseRequestDto = components['schemas']['ReleaseRequestDto'];

function toBody(draft: ReleaseDraft): ReleaseRequestDto {
    return {
        title: draft.title,
        version: draft.version,
        type: draft.type,
        releaseDate: draft.releaseDate,
        description: draft.description,
        hardware: draft.hardware,
        credits: draft.credits.map(({id, type, roles}) => ({id, type, roles})),
    };
}

/** The release and, when one is chosen, its new archive, as the parts of a multipart form. */
function toForm(draft: ReleaseDraft): {readonly release: ReleaseRequestDto; readonly archive?: Blob} {
    return {release: toBody(draft), ...(draft.archive === undefined ? {} : {archive: draft.archive})};
}

/** A release as it states itself: `GET /api/software/{id}/releases/{releaseId}`. */
export async function fetchSoftwareRelease(softwareId: string, releaseId: string, signal: AbortSignal): Promise<ReleaseDetails> {
    const {data, error, response} = await api.GET('/api/software/{softwareId}/releases/{releaseId}', {params: {path: {softwareId, releaseId}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** The parsed tree rooted at a release's downloadable file; null while the file waits for parsing. */
export async function fetchReleaseFiles(softwareId: string, releaseId: string, signal: AbortSignal): Promise<ReleaseFile | null> {
    const {data, error, response} = await api.GET('/api/software/{softwareId}/releases/{releaseId}/files', {params: {path: {softwareId, releaseId}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data.root;
}

/** Builds the tree of a release file again; only whoever may change the release may ask. */
export async function reparseReleaseFiles(softwareId: string, releaseId: string): Promise<void> {
    const {error, response} = await api.POST('/api/software/{softwareId}/releases/{releaseId}/files/reparse', {params: {path: {softwareId, releaseId}}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

/** Adds a release to its software and returns its id. */
export async function createRelease(softwareId: string, draft: ReleaseDraft): Promise<string> {
    const {data, error, response} = await api.POST('/api/software/{softwareId}/releases', {
        params: {path: {softwareId}},
        body: toForm(draft),
        bodySerializer: multipartForm,
    });
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data.id;
}

export async function updateRelease(softwareId: string, releaseId: string, draft: ReleaseDraft): Promise<void> {
    const {error, response} = await api.PUT('/api/software/{softwareId}/releases/{releaseId}', {
        params: {path: {softwareId, releaseId}, query: {removeArchive: draft.removeArchive ?? null}},
        body: toForm(draft),
        bodySerializer: multipartForm,
    });
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

export async function deleteRelease(softwareId: string, releaseId: string): Promise<void> {
    const {error, response} = await api.DELETE('/api/software/{softwareId}/releases/{releaseId}', {params: {path: {softwareId, releaseId}}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

/** The hardware of a platform, to choose from: `GET /api/hardware?platform={code}`. */
export async function fetchPlatformHardware(platform: string, signal: AbortSignal): Promise<readonly Hardware[]> {
    const {data, error, response} = await api.GET('/api/hardware', {params: {query: {platform}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}
