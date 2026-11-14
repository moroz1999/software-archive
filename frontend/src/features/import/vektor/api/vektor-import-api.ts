import {getJson, mapResponse, postNoContent, readStringArray} from '@/shared/api';

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
