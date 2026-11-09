import {deleteResource, getJson, mapResponse, postJson, putJson, readNullableString, readObject, readString} from '@/shared/api';
import {type SoftwareDetails, type SoftwareId, toReleaseDate, toSoftwareId, toSoftwareTitle} from '../model/software';
import type {SoftwareDraft} from '../model/software-form';

function softwarePath(id: SoftwareId): string {
    return `/api/software/${id}`;
}

function toBody(draft: SoftwareDraft): unknown {
    return {title: draft.title, description: draft.description, releaseDate: draft.releaseDate};
}

/** One software with its details: `GET /api/software/{id}`. */
export async function fetchSoftware(id: SoftwareId, signal: AbortSignal): Promise<SoftwareDetails> {
    const json = await getJson(softwarePath(id), signal);
    return mapResponse(() => {
        const dto = readObject(json, 'software');
        const releaseDate = readNullableString(dto, 'releaseDate');
        return {
            id: toSoftwareId(readString(dto, 'id')),
            title: toSoftwareTitle(readString(dto, 'title')),
            description: readNullableString(dto, 'description'),
            releaseDate: releaseDate === null ? null : toReleaseDate(releaseDate),
        };
    });
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
