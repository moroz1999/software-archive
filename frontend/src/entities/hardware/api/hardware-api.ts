import {api, apiError} from '@/shared/api';
import {SUPPORTED_UI_LANGUAGES, type UiLanguage} from '@/shared/lib/i18n';
import {type HardwareDetails, type HardwareGroupItem, type HardwareKind, type HardwareNames} from '../model/hardware';
import type {HardwareDraft} from '../model/hardware-form';

/** The names in the languages of the site; one in another language is left out, hardware without a Russian name is broken. */
function toNames(dto: Readonly<Record<string, string>>): HardwareNames {
    const names: Partial<Record<UiLanguage, string>> = {};
    for (const language of SUPPORTED_UI_LANGUAGES) {
        const name = dto[language];
        if (name !== undefined) {
            names[language] = name;
        }
    }
    const {ru} = dto;
    if (ru === undefined) {
        throw new TypeError('Hardware has a Russian name.');
    }
    return {...names, ru};
}

/** Every group in its order with its hardware: `GET /api/hardware-groups`. */
export async function fetchHardwareGroups(signal: AbortSignal): Promise<readonly HardwareGroupItem[]> {
    const {data, error, response} = await api.GET('/api/hardware-groups', {signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** One group or item of hardware with its names in every language: `GET /api/hardware-groups/{code}`, `GET /api/hardware/{code}`. */
export async function fetchHardwareDetails(kind: HardwareKind, code: string, signal: AbortSignal): Promise<HardwareDetails> {
    if (kind === 'group') {
        const {data, error, response} = await api.GET('/api/hardware-groups/{code}', {params: {path: {code}}, signal});
        if (error !== undefined) {
            throw apiError(response, error);
        }
        return {kind, code: data.code, names: toNames(data.names), group: null, platforms: []};
    }
    const {data, error, response} = await api.GET('/api/hardware/{code}', {params: {path: {code}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return {kind, code: data.code, names: toNames(data.names), group: data.group, platforms: data.platforms};
}

/** Adds a group or an item of hardware and returns its code. */
export async function createHardware(kind: HardwareKind, draft: HardwareDraft): Promise<string> {
    const {data, error, response} = kind === 'group'
        ? await api.POST('/api/hardware-groups', {body: {code: draft.code, names: draft.names}})
        : await api.POST('/api/hardware', {body: {code: draft.code, names: draft.names, group: draft.group, platforms: draft.platforms}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data.code;
}

/** A group is sent with its names; an item with its group and platforms too. The code goes only with an addition. */
export async function updateHardware(kind: HardwareKind, code: string, draft: HardwareDraft): Promise<void> {
    const {error, response} = kind === 'group'
        ? await api.PUT('/api/hardware-groups/{code}', {params: {path: {code}}, body: {code: null, names: draft.names}})
        : await api.PUT('/api/hardware/{code}', {params: {path: {code}}, body: {code: null, names: draft.names, group: draft.group, platforms: draft.platforms}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

/** Deletes a group with no hardware, or an item no software and no release has. */
export async function deleteHardware(kind: HardwareKind, code: string): Promise<void> {
    const {error, response} = kind === 'group'
        ? await api.DELETE('/api/hardware-groups/{code}', {params: {path: {code}}})
        : await api.DELETE('/api/hardware/{code}', {params: {path: {code}}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}
