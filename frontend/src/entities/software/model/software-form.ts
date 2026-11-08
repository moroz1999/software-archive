import {type TitleProblem, titleProblem} from '../../../shared/lib';
import {type ReleaseDate, type SoftwareDetails, type SoftwareTitle, toReleaseDate, toSoftwareTitle} from './software';

/** What the software form holds while it is being filled: raw text of every field. */
export interface SoftwareFormValues {
    readonly title: string;
    readonly description: string;
    /** ISO `YYYY-MM-DD` from a date input, or empty. */
    readonly releaseDate: string;
}

/** Software to be created or saved, as the form produced it. */
export interface SoftwareDraft {
    readonly title: SoftwareTitle;
    readonly description: string | null;
    readonly releaseDate: ReleaseDate | null;
}

export interface SoftwareFormErrors {
    readonly title?: TitleProblem;
    readonly releaseDate?: 'invalid';
}

export type SoftwareFormResult =
    | {readonly ok: true; readonly draft: SoftwareDraft}
    | {readonly ok: false; readonly errors: SoftwareFormErrors};

const ISO_DATE = /^\d{4}-\d{2}-\d{2}$/;

/** Checks the format rules of the form (docs/domain/software.md); empty optional fields become unknown. */
export function validateSoftwareForm(values: SoftwareFormValues): SoftwareFormResult {
    const title = titleProblem(values.title);
    const releaseDateInvalid = values.releaseDate !== '' && !ISO_DATE.test(values.releaseDate);
    if (title !== null || releaseDateInvalid) {
        return {ok: false, errors: {...(title === null ? {} : {title}), ...(releaseDateInvalid ? {releaseDate: 'invalid'} : {})}};
    }
    const description = values.description.trim();
    return {
        ok: true,
        draft: {
            title: toSoftwareTitle(values.title),
            description: description === '' ? null : values.description,
            releaseDate: values.releaseDate === '' ? null : toReleaseDate(values.releaseDate),
        },
    };
}

/** The form values of existing software, or of an empty form. */
export function toSoftwareFormValues(software?: SoftwareDetails): SoftwareFormValues {
    return {
        title: software?.title ?? '',
        description: software?.description ?? '',
        releaseDate: software?.releaseDate ?? '',
    };
}
