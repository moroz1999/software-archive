import {api, apiError, multipartForm} from '@/shared/api';
import {type Gallery, type Screenshot} from '../model/screenshot';

/** The screenshots of the software itself, or of its release when one is given. */
export async function fetchScreenshots(softwareId: string, releaseId: string | null, signal: AbortSignal, gallery: Gallery = 'screenshots'): Promise<readonly Screenshot[]> {
    const {data, error, response} = releaseId === null
        ? await api.GET('/api/software/{softwareId}/{gallery}', {params: {path: {softwareId, gallery}}, signal})
        : await api.GET('/api/software/{softwareId}/releases/{releaseId}/screenshots', {params: {path: {softwareId, releaseId}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** Uploads the files as screenshots after those there are and returns the new ones. */
export async function uploadScreenshots(softwareId: string, releaseId: string | null, files: readonly File[], gallery: Gallery = 'screenshots'): Promise<readonly Screenshot[]> {
    const {data, error, response} = releaseId === null
        ? await api.POST('/api/software/{softwareId}/{gallery}', {params: {path: {softwareId, gallery}}, body: {files}, bodySerializer: multipartForm})
        : await api.POST('/api/software/{softwareId}/releases/{releaseId}/screenshots', {params: {path: {softwareId, releaseId}}, body: {files}, bodySerializer: multipartForm});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

export async function deleteScreenshot(softwareId: string, releaseId: string | null, screenshotId: string, gallery: Gallery = 'screenshots'): Promise<void> {
    const {error, response} = releaseId === null
        ? await api.DELETE('/api/software/{softwareId}/{gallery}/{screenshotId}', {params: {path: {softwareId, gallery, screenshotId}}})
        : await api.DELETE('/api/software/{softwareId}/releases/{releaseId}/screenshots/{screenshotId}', {params: {path: {softwareId, releaseId, screenshotId}}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

/** Puts every screenshot in the order given. */
export async function reorderScreenshots(softwareId: string, releaseId: string | null, order: readonly string[], gallery: Gallery = 'screenshots'): Promise<void> {
    const body = {order};
    const {error, response} = releaseId === null
        ? await api.PUT('/api/software/{softwareId}/{gallery}/order', {params: {path: {softwareId, gallery}}, body})
        : await api.PUT('/api/software/{softwareId}/releases/{releaseId}/screenshots/order', {params: {path: {softwareId, releaseId}}, body});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}
