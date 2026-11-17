import {readNullableString, readObject, readString} from './parse';

/** Who added an entry of the archive and when: `by` is the user name, null once their account was deleted. */
export interface Addition {
    readonly by: string | null;
    /** The moment it was added, ISO-8601. */
    readonly at: string;
}

/** The `added` field of an entry's details. */
export function readAddition(raw: unknown): Addition {
    const dto = readObject(raw, 'added');
    return {by: readNullableString(dto, 'by'), at: readString(dto, 'at')};
}
