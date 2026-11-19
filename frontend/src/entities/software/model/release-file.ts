import type {Brand} from '@/shared/lib/brand';
import {isUuid} from '@/shared/lib/uuid';

export type ReleaseFileId = Brand<string, 'ReleaseFileId'>;

export interface ReleaseFile {
    readonly id: ReleaseFileId;
    readonly name: string;
    readonly directory: boolean;
    readonly executable: boolean;
    /** The file could not be parsed whole, so its contents are not known. */
    readonly unreadable: boolean;
    readonly children: readonly ReleaseFile[];
}

export function toReleaseFileId(raw: string): ReleaseFileId {
    if (!isUuid(raw)) {
        throw new TypeError(`Invalid release file id: ${raw}`);
    }
    return raw as ReleaseFileId;
}
