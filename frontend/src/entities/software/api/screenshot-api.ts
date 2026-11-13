import {deleteResource, getJson, mapResponse, postForm, putJson, readArray, readObject, readString} from '@/shared/api';
import type {ReleaseId} from '../model/release';
import {type Screenshot, type ScreenshotId, toScreenshotId} from '../model/screenshot';
import type {SoftwareId} from '../model/software';

/** The screenshots of the software itself, or of its release when one is given. */
function screenshotsPath(softwareId: SoftwareId, releaseId: ReleaseId | null): string {
    return releaseId === null
        ? `/api/software/${softwareId}/screenshots`
        : `/api/software/${softwareId}/releases/${releaseId}/screenshots`;
}

/** The screenshots in an answer; missing means none. */
export function parseScreenshots(raw: unknown): readonly Screenshot[] {
    if (raw === undefined) {
        return [];
    }
    return readArray(raw, 'screenshots').map((item) => {
        const dto = readObject(item, 'screenshot');
        return {id: toScreenshotId(readString(dto, 'id')), url: readString(dto, 'url')};
    });
}

/** The screenshots of the software itself, or of its release when one is given. */
export async function fetchScreenshots(softwareId: SoftwareId, releaseId: ReleaseId | null, signal: AbortSignal): Promise<readonly Screenshot[]> {
    const json = await getJson(screenshotsPath(softwareId, releaseId), signal);
    return mapResponse(() => parseScreenshots(json));
}

/** Uploads the files as screenshots after those there are and returns the new ones. */
export async function uploadScreenshots(softwareId: SoftwareId, releaseId: ReleaseId | null, files: readonly File[]): Promise<readonly Screenshot[]> {
    const form = new FormData();
    files.forEach((file) => {
        form.append('files', file);
    });
    const json = await postForm(screenshotsPath(softwareId, releaseId), form);
    return mapResponse(() => parseScreenshots(json));
}

export async function deleteScreenshot(softwareId: SoftwareId, releaseId: ReleaseId | null, id: ScreenshotId): Promise<void> {
    await deleteResource(`${screenshotsPath(softwareId, releaseId)}/${id}`);
}

/** Puts every screenshot in the order given. */
export async function reorderScreenshots(softwareId: SoftwareId, releaseId: ReleaseId | null, order: readonly ScreenshotId[]): Promise<void> {
    await putJson(`${screenshotsPath(softwareId, releaseId)}/order`, {order});
}
