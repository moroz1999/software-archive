import {deleteResource, getJson, mapResponse, postJson, putJson, readAddition, readArray, readBoolean, readNullableString, readObject, readString} from '@/shared/api';
import {type SoftwareRelease, toReleaseId, toReleaseType} from '../model/release';
import {
    type Hardware,
    type SoftwareDetails,
    type SoftwareId,
    type SoftwareReference,
    SOFTWARE_REFERENCE_TYPES,
    type SoftwareReferenceType,
    toReleaseDate,
    toSoftwarePlatformCode,
    toSoftwareReferenceType,
    toSoftwareId,
    toPreservationStatus,
    toSoftwareTitle,
} from '../model/software';
import type {SoftwareDraft} from '../model/software-form';
import type {EntityAutocompleteOption} from '@/shared/ui/multi-entity-autocomplete';
import {parseScreenshots} from './screenshot-api';
import {parseReleaseArchive} from './release-api';

function softwarePath(id: SoftwareId): string {
    return `/api/software/${id}`;
}

function toBody(draft: SoftwareDraft): unknown {
    return {
        title: draft.title,
        description: draft.description,
        releaseDate: draft.releaseDate,
        platform: draft.platform,
        hardware: draft.hardware,
        categories: draft.categories,
        credits: draft.credits.map(({id, type, roles}) => ({id, type, roles})),
        preservation: draft.preservation,
    };
}

function parseSoftwareReferences(raw: unknown, name: string): readonly SoftwareReference[] {
    return readArray(raw, name).map((item) => {
        const reference = readObject(item, `${name} item`);
        return {
            id: readString(reference, 'id'),
            type: toSoftwareReferenceType(readString(reference, 'type')),
            title: readString(reference, 'title'),
            ...(reference['ownerId'] === undefined ? {} : {ownerId: readNullableString(reference, 'ownerId')}),
        };
    });
}

/** The hardware of a release or software, or of a platform's reference book. */
export function parseHardwareList(raw: unknown): readonly Hardware[] {
    return readArray(raw, 'hardware').map((item) => {
        const hardware = readObject(item, 'hardware item');
        return {code: readString(hardware, 'code'), name: readString(hardware, 'name'), group: readString(hardware, 'group'), groupName: readString(hardware, 'groupName')};
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
            categories: readArray(dto['categories'], 'categories').map((item) => {
                const category = readObject(item, 'category');
                return {
                    id: readString(category, 'id'),
                    title: readString(category, 'title'),
                    branch: readArray(category['branch'], 'branch').map((raw) => {
                        const step = readObject(raw, 'category step');
                        return {id: readString(step, 'id'), title: readString(step, 'title')};
                    }),
                };
            }),
            credits: parseSoftwareCredits(dto['credits']),
            sources: readArray(dto['sources'], 'sources').map((item) => {
                const source = readObject(item, 'source');
                return {archive: readString(source, 'archive'), url: readString(source, 'url')};
            }),
            preservation: toPreservationStatus(readString(dto, 'preservation')),
            added: readAddition(dto['added']),
            canChange: readBoolean(dto, 'canChange'),
        };
    });
}

function parseSoftwareCredits(raw: unknown): NonNullable<SoftwareDetails['credits']> {
    return readArray(raw, 'credits').map((item) => {
        const credit = readObject(item, 'credit');
        return {
            id: readString(credit, 'id'),
            type: toSoftwareReferenceType(readString(credit, 'type')),
            title: readString(credit, 'title'),
            ...(credit['ownerId'] === undefined ? {} : {ownerId: readNullableString(credit, 'ownerId')}),
            roles: readArray(credit['roles'], 'credit roles').map((role) => {
                if (typeof role !== 'string') {
                    throw new TypeError('A credit role must be a string.');
                }
                return role;
            }),
        };
    });
}

/** Finds people, groups and their labels for one typed multi-select. */
export async function fetchSoftwareReferences(
    types: readonly SoftwareReferenceType[],
    query: string,
    signal: AbortSignal,
): Promise<readonly SoftwareReference[]> {
    const params = new URLSearchParams({types: types.join(','), query});
    const json = await getJson(`/api/entity-lookup?${params.toString()}`, signal);
    return mapResponse(() => parseSoftwareReferences(readObject(json, 'software reference lookup')['items'], 'items'));
}

/** Finds people, groups and the labels of either for the credits of software. */
export async function fetchSoftwareParticipants(query: string, signal: AbortSignal): Promise<readonly EntityAutocompleteOption[]> {
    return fetchSoftwareReferences(SOFTWARE_REFERENCE_TYPES, query, signal);
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
            screenshots: parseScreenshots(dto['screenshots']),
            archive: parseReleaseArchive(dto['archive']),
            added: readAddition(dto['added']),
            canChange: readBoolean(dto, 'canChange'),
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
