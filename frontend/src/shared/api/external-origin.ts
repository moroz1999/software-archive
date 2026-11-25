import {readArray, readNullableString, readObject, readString} from './parse';

/**
 * An import an entry came from (docs/domain/imports.md#origin): the source archive, the id the entry has there — or
 * the name it is known by there, when the source gives it no id — and its page there, null when it has none.
 */
export interface ExternalOrigin {
    readonly origin: string;
    readonly externalId: string;
    readonly url: string | null;
}

/** The `origins` field of an entry's details. */
export function readOrigins(raw: unknown): readonly ExternalOrigin[] {
    return readArray(raw, 'origins').map((item) => {
        const dto = readObject(item, 'origin');
        return {origin: readString(dto, 'origin'), externalId: readString(dto, 'externalId'), url: readNullableString(dto, 'url')};
    });
}
