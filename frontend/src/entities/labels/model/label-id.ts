import {NotFoundError} from '@/shared/api';
import {type Brand} from '@/shared/lib/brand';
import {isUuid} from '@/shared/lib/uuid';

/** The id of a label: a name works are released under. */
export type LabelId = Brand<string, 'LabelId'>;

export function toLabelId(raw: string): LabelId {
    if (!isUuid(raw)) {
        throw new TypeError(`Invalid label id: ${raw}`);
    }
    return raw as LabelId;
}

/** The id of a page's label from its URL; one that cannot be an id means the page does not exist. */
export function requireLabelId(raw: string | undefined): LabelId {
    if (raw === undefined || !isUuid(raw)) {
        throw new NotFoundError('label.not-found');
    }
    return toLabelId(raw);
}
