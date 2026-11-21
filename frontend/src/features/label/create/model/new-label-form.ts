import type {NewLabelDraft} from '@/entities/labels';
import {locationProblem, type LocationValue, toLocationDraft} from '@/entities/locations';
import {type TitleProblem, titleProblem} from '@/shared/lib/form-rules';

/** What the form of a new label holds while it is being filled. */
export interface NewLabelFormValues {
    readonly name: string;
    readonly locations: readonly LocationValue[];
}

export interface NewLabelFormErrors {
    readonly name?: TitleProblem;
    /** The problem of each location by its row; null for a location without one. */
    readonly locations?: readonly ('country-required' | null)[];
}

export const EMPTY_NEW_LABEL: NewLabelFormValues = {name: '', locations: []};

/** The format errors of the values; none means they are sent as the draft. */
export function validateNewLabelForm(values: NewLabelFormValues): {readonly ok: true; readonly draft: NewLabelDraft} | {readonly ok: false; readonly errors: NewLabelFormErrors} {
    const name = titleProblem(values.name);
    const locations = values.locations.map(locationProblem);
    const locationsWrong = locations.some((problem) => problem !== null);
    if (name !== null || locationsWrong) {
        return {ok: false, errors: {...(name === null ? {} : {name}), ...(locationsWrong ? {locations} : {})}};
    }
    return {
        ok: true,
        draft: {
            name: values.name,
            locations: values.locations.map(toLocationDraft),
        },
    };
}
