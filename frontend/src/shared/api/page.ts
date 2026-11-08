import {readArray, readNumber, readObject} from './parse';

/** One page of a paged backend list. */
export interface Page<T> {
    readonly items: readonly T[];
    readonly number: number;
    readonly size: number;
    readonly totalElements: number;
    readonly totalPages: number;
}

/** Parses the backend page contract `{content: [...], page: {size, number, totalElements, totalPages}}`. */
export function parsePage<T>(json: unknown, parseItem: (item: unknown) => T): Page<T> {
    const body = readObject(json, 'page response');
    const meta = readObject(body['page'], 'page metadata');
    return {
        items: readArray(body['content'], 'page content').map(parseItem),
        number: readNumber(meta, 'number'),
        size: readNumber(meta, 'size'),
        totalElements: readNumber(meta, 'totalElements'),
        totalPages: readNumber(meta, 'totalPages'),
    };
}
