import {deleteResource, getJson, mapResponse, postJson, putJson, readArray, readObject, readString} from '@/shared/api';
import {SUPPORTED_UI_LANGUAGES, type UiLanguage} from '@/shared/lib/i18n';
import {
    type HardwareCode,
    type HardwareDetails,
    type HardwareGroupCode,
    type HardwareGroupItem,
    type HardwareKind,
    type HardwareNames,
    toHardwareCode,
    toHardwareGroupCode,
} from '../model/hardware';
import type {HardwareDraft} from '../model/hardware-form';

function collectionPath(kind: HardwareKind): string {
    return kind === 'group' ? '/api/hardware-groups' : '/api/hardware';
}

function itemPath(kind: HardwareKind, code: HardwareCode | HardwareGroupCode): string {
    return `${collectionPath(kind)}/${code}`;
}

/** The names in the languages of the site; one in another language is left out, a missing Russian one is malformed. */
function parseNames(value: unknown): HardwareNames {
    const dto = readObject(value, 'names');
    const names: Partial<Record<UiLanguage, string>> = {};
    for (const language of SUPPORTED_UI_LANGUAGES) {
        if (dto[language] !== undefined) {
            names[language] = readString(dto, language);
        }
    }
    return {...names, ru: readString(dto, 'ru')};
}

function parseGroup(item: unknown): HardwareGroupItem {
    const dto = readObject(item, 'group');
    return {
        code: toHardwareGroupCode(readString(dto, 'code')),
        name: readString(dto, 'name'),
        hardware: readArray(dto['hardware'], 'hardware').map((entry) => {
            const hardware = readObject(entry, 'hardware');
            return {code: toHardwareCode(readString(hardware, 'code')), name: readString(hardware, 'name')};
        }),
    };
}

/** Every group in its order with its hardware: `GET /api/hardware-groups`. */
export async function fetchHardwareGroups(signal: AbortSignal): Promise<readonly HardwareGroupItem[]> {
    const json = await getJson('/api/hardware-groups', signal);
    return mapResponse(() => readArray(json, 'groups').map(parseGroup));
}

/** One group or item of hardware with its names in every language: `GET /api/hardware-groups/{code}`, `GET /api/hardware/{code}`. */
export async function fetchHardwareDetails(kind: HardwareKind, code: HardwareCode | HardwareGroupCode, signal: AbortSignal): Promise<HardwareDetails> {
    const json = await getJson(itemPath(kind, code), signal);
    return mapResponse(() => {
        const dto = readObject(json, kind);
        const raw = readString(dto, 'code');
        return {
            kind,
            code: kind === 'group' ? toHardwareGroupCode(raw) : toHardwareCode(raw),
            names: parseNames(dto['names']),
            group: kind === 'group' ? null : toHardwareGroupCode(readString(dto, 'group')),
            platforms: kind === 'group' ? [] : readArray(dto['platforms'], 'platforms').map((platform) => {
                if (typeof platform !== 'string') {
                    throw new TypeError('A platform is a code.');
                }
                return platform;
            }),
        };
    });
}

/** A group is sent with its names; an item with its group and platforms too. The code goes only with an addition. */
function toBody(kind: HardwareKind, draft: HardwareDraft, withCode: boolean): unknown {
    const code = withCode ? {code: draft.code} : {};
    return kind === 'group'
        ? {...code, names: draft.names}
        : {...code, names: draft.names, group: draft.group, platforms: draft.platforms};
}

/** Adds a group or an item of hardware and returns its code. */
export async function createHardware(kind: HardwareKind, draft: HardwareDraft): Promise<string> {
    const json = await postJson(collectionPath(kind), toBody(kind, draft, true));
    return mapResponse(() => readString(readObject(json, 'created hardware'), 'code'));
}

export async function updateHardware(kind: HardwareKind, code: HardwareCode | HardwareGroupCode, draft: HardwareDraft): Promise<void> {
    await putJson(itemPath(kind, code), toBody(kind, draft, false));
}

/** Deletes a group with no hardware, or an item no software and no release has. */
export async function deleteHardware(kind: HardwareKind, code: HardwareCode | HardwareGroupCode): Promise<void> {
    await deleteResource(itemPath(kind, code));
}
