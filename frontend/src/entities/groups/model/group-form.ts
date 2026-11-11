import {type TitleProblem, titleProblem} from '@/shared/lib/title';
import type {GroupDetails} from './group';

/** A label in the form: a new one has no id yet. */
export interface GroupFormLabel {
    readonly id: string | null;
    readonly name: string;
    readonly display: boolean;
}

/** A location in the form: the ids of the chosen country and city, each empty while not chosen. */
export interface GroupFormLocation {
    readonly country: string;
    readonly city: string;
    readonly primary: boolean;
}

/** What the group form holds while it is being filled. */
export interface GroupFormValues {
    readonly name: string;
    readonly notes: string;
    /** The codes of the chosen types, in the order they were chosen. */
    readonly types: readonly string[];
    readonly labels: readonly GroupFormLabel[];
    readonly locations: readonly GroupFormLocation[];
}

/** A group to be created or saved, as the form produced it. */
export interface GroupDraft {
    readonly name: string;
    readonly notes: string | null;
    readonly types: readonly string[];
    readonly labels: readonly GroupFormLabel[];
    readonly locations: readonly {readonly country: string; readonly city: string | null; readonly primary: boolean}[];
}

export interface GroupFormErrors {
    readonly name?: TitleProblem;
    readonly types?: 'required';
    /** The problem of each label by its row; null for a label without one. */
    readonly labels?: readonly (TitleProblem | null)[];
    /** The problem of each location by its row; null for a location without one. */
    readonly locations?: readonly ('country-required' | null)[];
}

export type GroupFormResult =
    | {readonly ok: true; readonly draft: GroupDraft}
    | {readonly ok: false; readonly errors: GroupFormErrors};

/**
 * Checks the format rules of the form (docs/domain/authors.md): a group has a name and at least one type, every label
 * has a name and every location a country. Empty optional fields become unknown.
 */
export function validateGroupForm(values: GroupFormValues): GroupFormResult {
    const name = titleProblem(values.name) ?? undefined;
    const types = values.types.length === 0 ? 'required' : undefined;
    const labels = values.labels.map((label) => titleProblem(label.name));
    const locations = values.locations.map((location) => (location.country === '' ? 'country-required' : null));
    const labelsWrong = labels.some((problem) => problem !== null);
    const locationsWrong = locations.some((problem) => problem !== null);
    if (name !== undefined || types !== undefined || labelsWrong || locationsWrong) {
        return {
            ok: false,
            errors: {
                ...(name === undefined ? {} : {name}),
                ...(types === undefined ? {} : {types}),
                ...(labelsWrong ? {labels} : {}),
                ...(locationsWrong ? {locations} : {}),
            },
        };
    }
    return {
        ok: true,
        draft: {
            name: values.name,
            notes: values.notes.trim() === '' ? null : values.notes,
            types: values.types,
            labels: values.labels,
            locations: values.locations.map((location) => ({
                country: location.country,
                city: location.city === '' ? null : location.city,
                primary: location.primary,
            })),
        },
    };
}

/** The form values of an existing group, or of an empty form. */
export function toGroupFormValues(group?: GroupDetails): GroupFormValues {
    return {
        name: group?.groupName ?? '',
        notes: group?.notes ?? '',
        types: group?.types ?? [],
        labels: group?.labels.map((label) => ({id: label.id, name: label.name, display: label.display})) ?? [],
        locations: group?.locations.map((location) => ({
            country: location.countryId ?? '',
            city: location.cityId ?? '',
            primary: location.primary,
        })) ?? [],
    };
}
