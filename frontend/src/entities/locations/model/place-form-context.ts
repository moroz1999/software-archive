import {createRequiredContext} from '@/shared/lib/context';
import type {UiLanguage} from '@/shared/lib/i18n';
import type {PlaceKind} from './geography';
import type {PlaceFormErrors, PlaceFormValues} from './place-form';

export interface PlaceFormState {
    /** A country or a city: only a city has a country. */
    readonly kind: PlaceKind;
    readonly values: PlaceFormValues;
    /** Format errors found on the last submit; they clear once the form is edited. */
    readonly errors: PlaceFormErrors;
    /** Why the last submit was refused for the country of the city; it clears once the country is changed. */
    readonly countryRefusal: string | undefined;
    readonly changeName: (language: UiLanguage) => (value: string) => void;
    readonly changeLatitude: (value: string) => void;
    readonly changeLongitude: (value: string) => void;
    readonly changeCountry: (value: string) => void;
    /** Checks the format rules and hands the draft on only when they pass. */
    readonly submit: () => void;
    /** Saving runs: the form is locked. */
    readonly busy: boolean;
    /** Why the last submit was refused, for what no field takes: shown at the top of the form. */
    readonly error: string | undefined;
}

/** The form being filled in, for the fields inside a PlaceFormProvider. */
export const [PlaceFormContext, usePlaceForm] = createRequiredContext<PlaceFormState>('PlaceForm');
