import {type TitleProblem, titleProblem} from '@/shared/lib/title';
import {type ReleaseDetails, type ReleaseType, toReleaseType} from './release';
import {type ReleaseDate, toReleaseDate} from './software';

/** The longest version of a release (docs/domain/software.md). */
export const RELEASE_VERSION_MAX_LENGTH = 50;

/** What the release form holds while it is being filled: raw text of every field, the codes of the checked hardware. */
export interface ReleaseFormValues {
    readonly title: string;
    readonly version: string;
    /** The code of the chosen type, or empty. */
    readonly type: string;
    /** ISO `YYYY-MM-DD` from a date input, or empty. */
    readonly releaseDate: string;
    readonly description: string;
    readonly hardware: readonly string[];
}

/** A release to be added or saved, as the form produced it; a release without a title is called by its software's. */
export interface ReleaseDraft {
    readonly title: string | null;
    readonly version: string | null;
    readonly type: ReleaseType;
    readonly releaseDate: ReleaseDate | null;
    readonly description: string | null;
    readonly hardware: readonly string[];
}

export interface ReleaseFormErrors {
    readonly title?: Exclude<TitleProblem, 'required'>;
    readonly version?: 'too-long';
    readonly type?: 'required';
    readonly releaseDate?: 'invalid';
}

export type ReleaseFormResult =
    | {readonly ok: true; readonly draft: ReleaseDraft}
    | {readonly ok: false; readonly errors: ReleaseFormErrors};

/** Checks the format rules of the form (docs/domain/software.md); empty optional fields become unknown. */
export function validateReleaseForm(values: ReleaseFormValues): ReleaseFormResult {
    const title = values.title.trim() === '' ? null : titleProblem(values.title);
    const versionTooLong = values.version.length > RELEASE_VERSION_MAX_LENGTH;
    const typeMissing = values.type === '';
    const releaseDateInvalid = values.releaseDate !== '' && !ISO_DATE.test(values.releaseDate);
    if (title === 'too-long' || versionTooLong || typeMissing || releaseDateInvalid) {
        return {
            ok: false,
            errors: {
                ...(title === 'too-long' ? {title} : {}),
                ...(versionTooLong ? {version: 'too-long'} : {}),
                ...(typeMissing ? {type: 'required'} : {}),
                ...(releaseDateInvalid ? {releaseDate: 'invalid'} : {}),
            },
        };
    }
    return {
        ok: true,
        draft: {
            title: orNull(values.title),
            version: orNull(values.version),
            type: toReleaseType(values.type),
            releaseDate: values.releaseDate === '' ? null : toReleaseDate(values.releaseDate),
            description: orNull(values.description),
            hardware: values.hardware,
        },
    };
}

const ISO_DATE = /^\d{4}-\d{2}-\d{2}$/;

/** An empty or blank optional field is unknown. */
function orNull(value: string): string | null {
    return value.trim() === '' ? null : value;
}

/** The form values of an existing release, or of a new one: of unknown type. */
export function toReleaseFormValues(release?: ReleaseDetails): ReleaseFormValues {
    return {
        title: release?.title ?? '',
        version: release?.version ?? '',
        type: release?.type ?? 'unknown',
        releaseDate: release?.releaseDate ?? '',
        description: release?.description ?? '',
        hardware: release?.hardware.map((item) => item.code) ?? [],
    };
}
