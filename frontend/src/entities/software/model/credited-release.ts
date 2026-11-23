import type {CreditedOwner, OwnCredit} from './credited-software';
import type {ReleaseId, ReleaseType} from './release';
import type {Screenshot} from './screenshot';
import type {ReleaseDate, SoftwareId} from './software';

/** A release on the page of a person, a group or a label: the release, its software, and their credits on it. */
export interface CreditedRelease {
    readonly id: ReleaseId;
    readonly softwareId: SoftwareId;
    readonly softwareTitle: string;
    /** Its own title, or the title of its software. */
    readonly title: string;
    readonly version: string | null;
    readonly type: ReleaseType;
    readonly releaseDate: ReleaseDate | null;
    readonly screenshots: readonly Screenshot[];
    readonly credits: readonly OwnCredit[];
}

export type {CreditedOwner};
