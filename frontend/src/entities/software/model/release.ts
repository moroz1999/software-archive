import {type components, NotFoundError} from '@/shared/api';
import {isUuid} from '@/shared/lib/uuid';

/** The release types (docs/domain/software.md). */
export type ReleaseType = components['schemas']['ReleaseType'];

/** A release of software, under its own title or the title of its software, with the hardware it shows. */
export type SoftwareRelease = components['schemas']['SoftwareReleaseDto'];

/** A release as it states itself, to edit it: its own title, if any, only its own hardware, and its credits. */
export type ReleaseDetails = components['schemas']['ReleaseDetailsDto'];

/** The id of a page's release from its URL; one that cannot be an id means the page does not exist. */
export function requireReleaseId(raw: string | undefined): string {
    if (raw === undefined || !isUuid(raw)) {
        throw new NotFoundError('release.not-found');
    }
    return raw;
}
