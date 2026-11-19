import type {Brand} from '@/shared/lib/brand';
import {isUuid} from '@/shared/lib/uuid';

export type ReleaseFileId = Brand<string, 'ReleaseFileId'>;

export interface ReleaseFile {
    readonly id: ReleaseFileId;
    readonly name: string;
    readonly directory: boolean;
    readonly executable: boolean;
    readonly children: readonly ReleaseFile[];
}

export function toReleaseFileId(raw: string): ReleaseFileId {
    if (!isUuid(raw)) {
        throw new TypeError(`Invalid release file id: ${raw}`);
    }
    return raw as ReleaseFileId;
}
