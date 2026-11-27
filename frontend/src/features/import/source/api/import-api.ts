import {api, apiError} from '@/shared/api';
import type {ImportRun} from '../model/import-run';

/** The codes of the sections of vektor-06c.ru an import can be started for: `GET /api/imports/vektor/sections`. */
export async function fetchVektorSections(signal: AbortSignal): Promise<readonly string[]> {
    const {data, error, response} = await api.GET('/api/imports/vektor/sections', {signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** Starts the import of one section, or of the whole site without one; it goes on in the background. */
export async function startVektorImport(section: string | null): Promise<void> {
    const {error, response} = await api.POST('/api/imports/vektor', {params: {query: {section}}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

/** The categories of "Базис" whose cards can be imported. */
export async function fetchBasisCategories(signal: AbortSignal): Promise<readonly string[]> {
    const {data, error, response} = await api.GET('/api/imports/basis/categories', {signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** Starts the import of one category of "Базис", or of the whole card index without one. */
export async function startBasisImport(category: string | null): Promise<void> {
    const {error, response} = await api.POST('/api/imports/basis', {params: {query: {category}}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

/**
 * The report of the import started last, null before the first one: `GET /api/imports/last-run`; with the lines of its
 * log after the one given only, all of them without one.
 */
export async function fetchLastImportRun(after: number | undefined, signal: AbortSignal): Promise<ImportRun | null> {
    const {data, error, response} = await api.GET('/api/imports/last-run', {params: {query: {after: after ?? null}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data.run;
}
