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
import {
    type ImportLogEntry,
    type ImportLogLine,
    type ImportRun,
    isDownloadedFile,
    isImportRunState,
    isPlaceKind,
    isSkipReason,
    isSoftwareSupplement,
    isTakenAuthorKind,
} from '../model/import-run';

/** A code of a fixed list, refused when the client does not know it. */
function readCode<T extends string>(object: JsonObject, key: string, isCode: (raw: string) => raw is T): T {
    const raw = readString(object, key);
    if (!isCode(raw)) {
        throw new TypeError(`Unknown ${key}: ${raw}`);
    }
    return raw;
}

function readNullableNumber(object: JsonObject, key: string): number | null {
    return object[key] === null || object[key] === undefined ? null : readNumber(object, key);
}

const VEKTOR_IMPORT_PATH = '/api/imports/vektor';
const BASIS_IMPORT_PATH = '/api/imports/basis';

/** The codes of the sections of vektor-06c.ru an import can be started for: `GET /api/imports/vektor/sections`. */
export async function fetchVektorSections(signal: AbortSignal): Promise<readonly string[]> {
    const json = await getJson(`${VEKTOR_IMPORT_PATH}/sections`, signal);
    return mapResponse(() => readStringArray(json, 'vektor sections'));
}

/** Starts the import of one section, or of the whole site without one; it goes on in the background. */
export function startVektorImport(section: string | null): Promise<void> {
    return postNoContent(section === null ? VEKTOR_IMPORT_PATH : `${VEKTOR_IMPORT_PATH}?${new URLSearchParams({section}).toString()}`);
}

/** The categories of "Базис" whose cards can be imported. */
export async function fetchBasisCategories(signal: AbortSignal): Promise<readonly string[]> {
    const json = await getJson(`${BASIS_IMPORT_PATH}/categories`, signal);
    return mapResponse(() => readStringArray(json, 'Basis categories'));
}

/** Starts the import of one category of "Базис", or of the whole card index without one. */
export function startBasisImport(category: string | null): Promise<void> {
    return postNoContent(category === null ? BASIS_IMPORT_PATH : `${BASIS_IMPORT_PATH}?${new URLSearchParams({category}).toString()}`);
}

/**
 * The report of the import started last, null before the first one: `GET /api/imports/last-run`; with the lines of its
 * log after the one given only, all of them without one.
 */
export async function fetchLastImportRun(after: number | undefined, signal: AbortSignal): Promise<ImportRun | null> {
    const json = await getJson(after === undefined ? LAST_RUN_PATH : `${LAST_RUN_PATH}?${new URLSearchParams({after: String(after)}).toString()}`, signal);
    return mapResponse(() => {
        const run = readObject(json, 'last run')['run'];
        return run === null || run === undefined ? null : toImportRun(readObject(run, 'run'));
    });
}

const LAST_RUN_PATH = '/api/imports/last-run';

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
        progress: (() => {
            const progress = readObject(dto['progress'], 'progress');
            return {
                position: readNumber(progress, 'position'),
                total: readNullableNumber(progress, 'total'),
                url: readNullableString(progress, 'url'),
            };
        })(),
        log: readArray(dto['log'], 'log').map((item) => toLogLine(readObject(item, 'log line'))),
    };
}

function toLogLine(dto: JsonObject): ImportLogLine {
    return {seq: readNumber(dto, 'seq'), at: readNullableString(dto, 'at'), entry: toLogEntry(readString(dto, 'kind'), readObject(dto['entry'], 'log entry'))};
}

function toLogEntry(kind: string, entry: JsonObject): ImportLogEntry {
    switch (kind) {
        case 'list':
            return {
                kind,
                page: {url: readString(entry, 'url'), number: readNumber(entry, 'number'), pages: readNullableNumber(entry, 'pages'), programs: readNumber(entry, 'programs')},
            };
        case 'download':
            return {
                kind,
                download: {
                    externalId: readString(entry, 'externalId'),
                    title: readString(entry, 'title'),
                    file: readCode(entry, 'file', isDownloadedFile),
                    name: readString(entry, 'name'),
                    size: readNumber(entry, 'size'),
                },
            };
        case 'added':
            return {
                kind,
                program: {
                    externalId: readString(entry, 'externalId'),
                    softwareId: toSoftwareId(readString(entry, 'softwareId')),
                    title: readString(entry, 'title'),
                    exists: readBoolean(entry, 'exists'),
                },
            };
        case 'known':
            return {
                kind,
                program: {
                    externalId: readString(entry, 'externalId'),
                    softwareId: toSoftwareId(readString(entry, 'softwareId')),
                    title: readString(entry, 'title'),
                    exists: readBoolean(entry, 'exists'),
                    supplements: readStringArray(entry['supplements'], 'supplements').map((code) => {
                        if (!isSoftwareSupplement(code)) {
                            throw new TypeError(`Unknown supplement: ${code}`);
                        }
                        return code;
                    }),
                },
            };
        case 'skipped':
            return {
                kind,
                program: {
                    externalId: readString(entry, 'externalId'),
                    url: readString(entry, 'url'),
                    title: readString(entry, 'title'),
                    reason: readCode(entry, 'reason', isSkipReason),
                    detail: readString(entry, 'detail'),
                },
            };
        case 'failed':
            return {kind, program: {externalId: readString(entry, 'externalId'), url: readString(entry, 'url'), reason: readString(entry, 'reason')}};
        case 'author':
            return {
                kind,
                author: {
                    kind: readCode(entry, 'kind', isTakenAuthorKind),
                    id: readString(entry, 'id'),
                    name: readString(entry, 'name'),
                    created: readBoolean(entry, 'created'),
                    exists: readBoolean(entry, 'exists'),
                },
            };
        case 'place':
            return {kind, place: {kind: readCode(entry, 'kind', isPlaceKind), name: readString(entry, 'name')}};
        default:
            throw new TypeError(`Unknown kind of a log line: ${kind}`);
    }
}
