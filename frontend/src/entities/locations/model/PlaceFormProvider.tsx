import {type ReactNode, useState} from 'react';
import {type BusinessError, type BusinessMessages, splitRefusal} from '@/shared/api';
import type {UiLanguage} from '@/shared/lib/i18n';
import type {PlaceKind} from './geography';
import {type PlaceDraft, type PlaceFormErrors, type PlaceFormValues, toPlaceFormValues, validatePlaceForm} from './place-form';
import {PlaceFormContext, type PlaceFormState} from './place-form-context';

export interface PlaceFormProviderProps {
    readonly kind: PlaceKind;
    /** Called only with values that pass the format rules. */
    readonly onSubmit: (draft: PlaceDraft) => void;
    readonly initial?: PlaceFormValues;
    readonly busy?: boolean;
    /** Why the last submit was refused (docs/error-handling.md). */
    readonly error?: BusinessError | undefined;
    /** The slice's texts for the codes of its business errors. */
    readonly messages?: BusinessMessages;
    readonly children: ReactNode;
}

/** The request field the country of a city is sent as: a refusal names it so. */
const COUNTRY_FIELD = 'countryId';

/** The values of the form of a country or a city being filled in, their format rules and why the last submit was refused. */
export function PlaceFormProvider({kind, onSubmit, initial, busy = false, error, messages = {}, children}: PlaceFormProviderProps) {
    const [values, setValues] = useState<PlaceFormValues>(() => initial ?? toPlaceFormValues());
    const [errors, setErrors] = useState<PlaceFormErrors>({});
    // The country changed since the refusal arrived: its refusal no longer applies. A new refusal starts afresh.
    const [countryEdited, setCountryEdited] = useState<{readonly error: BusinessError | undefined; readonly edited: boolean}>({error, edited: false});
    if (countryEdited.error !== error) {
        setCountryEdited({error, edited: false});
    }
    const refusal = splitRefusal(error, messages, [COUNTRY_FIELD]);

    function change(patch: Partial<PlaceFormValues>) {
        setValues((current) => ({...current, ...patch}));
        setErrors({});
    }

    const state: PlaceFormState = {
        kind,
        values,
        errors,
        countryRefusal: countryEdited.edited ? undefined : refusal.fields[COUNTRY_FIELD],
        changeName: (language: UiLanguage) => (value) => {
            setValues((current) => ({...current, names: {...current.names, [language]: value}}));
            setErrors({});
        },
        changeLatitude: (latitude) => {
            change({latitude});
        },
        changeLongitude: (longitude) => {
            change({longitude});
        },
        changeCountry: (country) => {
            change({country});
            setCountryEdited((current) => ({...current, edited: true}));
        },
        submit: () => {
            const result = validatePlaceForm(values);
            if (result.ok) {
                setErrors({});
                onSubmit(result.draft);
            } else {
                setErrors(result.errors);
            }
        },
        busy,
        error: refusal.form,
    };

    return <PlaceFormContext value={state}>{children}</PlaceFormContext>;
}
