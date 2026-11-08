import {SystemError} from './errors';

export type JsonObject = Readonly<Record<string, unknown>>;

export function isObject(value: unknown): value is JsonObject {
    return typeof value === 'object' && value !== null && !Array.isArray(value);
}

function unexpected(what: string): SystemError {
    return new SystemError('response', `Unexpected response: ${what}`);
}

export function readObject(value: unknown, what: string): JsonObject {
    if (!isObject(value)) {
        throw unexpected(`${what} is not an object`);
    }
    return value;
}

export function readArray(value: unknown, what: string): readonly unknown[] {
    if (!Array.isArray(value)) {
        throw unexpected(`${what} is not an array`);
    }
    return value;
}

export function readString(object: JsonObject, key: string): string {
    const value = object[key];
    if (typeof value !== 'string') {
        throw unexpected(`"${key}" is not a string`);
    }
    return value;
}

export function readNullableString(object: JsonObject, key: string): string | null {
    const value = object[key];
    if (value === null || value === undefined) {
        return null;
    }
    if (typeof value !== 'string') {
        throw unexpected(`"${key}" is not a string`);
    }
    return value;
}

export function readNumber(object: JsonObject, key: string): number {
    const value = object[key];
    if (typeof value !== 'number') {
        throw unexpected(`"${key}" is not a number`);
    }
    return value;
}

/** Runs a mapper over raw data; any failure means the server sent something this client does not understand. */
export function mapResponse<T>(map: () => T): T {
    try {
        return map();
    } catch (error) {
        if (error instanceof SystemError) {
            throw error;
        }
        throw new SystemError('response', 'Unexpected response', {cause: error});
    }
}
