import {toSoftwareId} from '@/entities/software';
import {
    getJson,
    type JsonObject,
    mapResponse,
    postNoContent,
    readArray,
    readBoolean,
    readNullableString,
    readNumber,
    readObject,
    readString,
    readStringArray,
} from '@/shared/api';
import {type ImportRun, isImportRunState} from '../model/import-run';

const VEKTOR_IMPORT_PATH = '/api/imports/vektor';

/** The codes of the sections of vektor-06c.ru an import can be started for: `GET /api/imports/vektor/sections`. */
export async function fetchVektorSections(signal: AbortSignal): Promise<readonly string[]> {
    const json = await getJson(`${VEKTOR_IMPORT_PATH}/sections`, signal);
    return mapResponse(() => readStringArray(json, 'vektor sections'));
}

/** Starts the import of one section, or of the whole site without one; it goes on in the background. */
export function startVektorImport(section: string | null): Promise<void> {
    return postNoContent(section === null ? VEKTOR_IMPORT_PATH : `${VEKTOR_IMPORT_PATH}?${new URLSearchParams({section}).toString()}`);
}

/** Starts the import of the whole card index "Базис" at caglrc.cc; it has no sections. */
export function startBasisImport(): Promise<void> {
    return postNoContent('/api/imports/basis');
}

/** The report of the import started last, null before the first one: `GET /api/imports/last-run`. */
export async function fetchLastImportRun(signal: AbortSignal): Promise<ImportRun | null> {
    const json = await getJson('/api/imports/last-run', signal);
    return mapResponse(() => {
        const run = readObject(json, 'last run')['run'];
        return run === null || run === undefined ? null : toImportRun(readObject(run, 'run'));
    });
}

function toImportRun(dto: JsonObject): ImportRun {
    const state = readString(dto, 'state');
    if (!isImportRunState(state)) {
        throw new TypeError(`Unknown import run state: ${state}`);
    }
    return {
        id: readString(dto, 'id'),
        archive: readString(dto, 'archive'),
        section: readNullableString(dto, 'section'),
        state,
        startedAt: readString(dto, 'startedAt'),
        endedAt: readNullableString(dto, 'endedAt'),
        added: readNumber(dto, 'added'),
        known: readNumber(dto, 'known'),
        skipped: readNumber(dto, 'skipped'),
        failed: readNumber(dto, 'failed'),
        addedPrograms: readArray(dto['addedPrograms'], 'added programs').map((item) => {
            const added = readObject(item, 'added program');
            return {
                externalId: readString(added, 'externalId'),
                softwareId: toSoftwareId(readString(added, 'softwareId')),
                title: readString(added, 'title'),
                exists: readBoolean(added, 'exists'),
            };
        }),
        failedPrograms: readArray(dto['failedPrograms'], 'failed programs').map((item) => {
            const failed = readObject(item, 'failed program');
            return {externalId: readString(failed, 'externalId'), url: readString(failed, 'url'), reason: readString(failed, 'reason')};
        }),
    };
}
