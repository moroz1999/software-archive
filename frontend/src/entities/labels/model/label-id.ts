import {NotFoundError} from '@/shared/api';
import {isUuid} from '@/shared/lib/uuid';


/** The id of a page's label from its URL; one that cannot be an id means the page does not exist. */
export function requireLabelId(raw: string | undefined): string {
    if (raw === undefined || !isUuid(raw)) {
        throw new NotFoundError('label.not-found');
    }
    return raw;
}
