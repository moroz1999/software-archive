import type {LabelDetails, LabelDraft, LabelOwnerOption} from '@/entities/labels';
import type {LocationValue} from '@/entities/locations';
import {type TitleProblem, titleProblem} from '@/shared/lib/title';

/** What the label form holds while it is being filled. */
export interface LabelFormValues {
    readonly name: string;
    /** Whose the label is to be; nobody's while none is chosen. */
    readonly owner: LabelOwnerOption | null;
    /** Whether a person chosen anew gets the label as their display name. */
    readonly display: boolean;
    readonly locations: readonly LocationValue[];
}

export interface LabelFormErrors {
    readonly name?: TitleProblem;
    /** The problem of each location by its row; null for a location without one. */
    readonly locations?: readonly ('country-required' | null)[];
}

/** The form starts from the label as it is: its owner chosen, its locations in their order. */
export function toLabelFormValues(label: LabelDetails): LabelFormValues {
    return {
        name: label.name,
        owner: label.owner === null ? null : {type: label.owner.type, id: label.owner.id, title: label.owner.name},
        display: false,
        locations: label.locations.map((location) => ({country: location.countryId, city: location.cityId ?? '', primary: location.primary})),
    };
}

/**
 * Whether the owner chosen is another person than the label has: only then the form asks whether the label becomes
 * their display name, as the display name among a person's own labels is chosen in the person's form.
 */
export function offersDisplay(values: LabelFormValues, label: LabelDetails): boolean {
    const {owner} = values;
    return owner?.type === 'author' && (label.owner?.type !== 'author' || label.owner.id !== owner.id);
}

/** The format errors of the values; none means they are sent as the draft. */
export function validateLabelForm(values: LabelFormValues, label: LabelDetails): {readonly ok: true; readonly draft: LabelDraft} | {readonly ok: false; readonly errors: LabelFormErrors} {
    const name = titleProblem(values.name);
    const locations = values.locations.map((location) => (location.country === '' ? 'country-required' : null));
    const locationsWrong = locations.some((problem) => problem !== null);
    if (name !== null || locationsWrong) {
        return {ok: false, errors: {...(name === null ? {} : {name}), ...(locationsWrong ? {locations} : {})}};
    }
    const {owner} = values;
    return {
        ok: true,
        draft: {
            name: values.name,
            owner: owner === null ? null : {type: owner.type, id: owner.id, display: offersDisplay(values, label) && values.display},
            locations: values.locations.map((location) => ({
                country: location.country,
                city: location.city === '' ? null : location.city,
                primary: location.primary,
            })),
        },
    };
}
