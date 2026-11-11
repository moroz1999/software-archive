import {type TitleProblem, titleProblem} from '@/shared/lib/title';
import type {AuthorDetails} from './author';

/** The longest real name the archive keeps. */
const REAL_NAME_MAX_LENGTH = 255;

/** A label in the form: a new one has no id yet. */
export interface AuthorFormLabel {
    readonly id: string | null;
    readonly name: string;
    readonly display: boolean;
}

/** A location in the form: the ids of the chosen country and city, each empty while not chosen. */
export interface AuthorFormLocation {
    readonly country: string;
    readonly city: string;
    readonly primary: boolean;
}

/** What the author form holds while it is being filled. */
export interface AuthorFormValues {
    readonly realName: string;
    readonly notes: string;
    readonly labels: readonly AuthorFormLabel[];
    readonly locations: readonly AuthorFormLocation[];
}

/** An author to be created or saved, as the form produced it. */
export interface AuthorDraft {
    readonly realName: string | null;
    readonly notes: string | null;
    readonly labels: readonly AuthorFormLabel[];
    readonly locations: readonly {readonly country: string; readonly city: string | null; readonly primary: boolean}[];
}

export interface AuthorFormErrors {
    readonly realName?: 'nameless' | 'too-long';
    /** The problem of each label by its row; null for a label without one. */
    readonly labels?: readonly (TitleProblem | null)[];
    /** The problem of each location by its row; null for a location without one. */
    readonly locations?: readonly ('country-required' | null)[];
}

export type AuthorFormResult =
    | {readonly ok: true; readonly draft: AuthorDraft}
    | {readonly ok: false; readonly errors: AuthorFormErrors};

function realNameProblem(values: AuthorFormValues): AuthorFormErrors['realName'] {
    if (values.realName.length > REAL_NAME_MAX_LENGTH) {
        return 'too-long';
    }
    return values.realName.trim() === '' && values.labels.length === 0 ? 'nameless' : undefined;
}

/**
 * Checks the format rules of the form (docs/domain/authors.md): an author is known by a real name or a label, every
 * label has a name and every location a country. Empty optional fields become unknown.
 */
export function validateAuthorForm(values: AuthorFormValues): AuthorFormResult {
    const realName = realNameProblem(values);
    const labels = values.labels.map((label) => titleProblem(label.name));
    const locations = values.locations.map((location) => (location.country === '' ? 'country-required' : null));
    const labelsWrong = labels.some((problem) => problem !== null);
    const locationsWrong = locations.some((problem) => problem !== null);
    if (realName !== undefined || labelsWrong || locationsWrong) {
        return {
            ok: false,
            errors: {
                ...(realName === undefined ? {} : {realName}),
                ...(labelsWrong ? {labels} : {}),
                ...(locationsWrong ? {locations} : {}),
            },
        };
    }
    return {
        ok: true,
        draft: {
            realName: values.realName.trim() === '' ? null : values.realName,
            notes: values.notes.trim() === '' ? null : values.notes,
            labels: values.labels,
            locations: values.locations.map((location) => ({
                country: location.country,
                city: location.city === '' ? null : location.city,
                primary: location.primary,
            })),
        },
    };
}

/** The form values of an existing author, or of an empty form. */
export function toAuthorFormValues(author?: AuthorDetails): AuthorFormValues {
    return {
        realName: author?.realName ?? '',
        notes: author?.notes ?? '',
        labels: author?.labels.map((label) => ({id: label.id, name: label.name, display: label.display})) ?? [],
        locations: author?.locations.map((location) => ({
            country: location.countryId ?? '',
            city: location.cityId ?? '',
            primary: location.primary,
        })) ?? [],
    };
}
