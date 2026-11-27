import {perUiLanguage, SUPPORTED_UI_LANGUAGES, type UiLanguage} from '@/shared/lib/i18n';
import type {PlaceDetails, PlaceNames} from './geography';

/**
 * What the form of a country or a city holds while it is filled: a name per language, empty when not given; the
 * coordinates as typed; a city's country, empty for none.
 */
export interface PlaceFormValues {
    readonly names: Readonly<Record<UiLanguage, string>>;
    readonly latitude: string;
    readonly longitude: string;
    readonly country: string;
}

/** A country or a city to be added or saved, as the form produced it: only the names given. */
export interface PlaceDraft {
    readonly names: PlaceNames;
    readonly countryId: string | null;
    readonly latitude: number | null;
    readonly longitude: number | null;
}

/** Why a coordinate is refused: not a number on the globe, or given without the other one. */
export type CoordinateProblem = 'latitude-range' | 'longitude-range' | 'pair';

export interface PlaceFormErrors {
    readonly nameRequired?: boolean;
    readonly latitude?: CoordinateProblem;
    readonly longitude?: CoordinateProblem;
}

export type PlaceFormResult =
    | {readonly ok: true; readonly draft: PlaceDraft}
    | {readonly ok: false; readonly errors: PlaceFormErrors};

/** The languages of the names in the order the form asks for them: Russian, which every place has, first. */
export const PLACE_NAME_LANGUAGES: readonly UiLanguage[] = ['ru', ...SUPPORTED_UI_LANGUAGES.filter((language) => language !== 'ru')];

/** A coordinate typed in: nothing for an empty field, NaN for what is not a number within the limit. */
function coordinate(text: string, limit: number): number | null {
    const trimmed = text.trim().replace(',', '.');
    if (trimmed === '') {
        return null;
    }
    const value = Number(trimmed);
    return Number.isFinite(value) && Math.abs(value) <= limit ? value : Number.NaN;
}

/**
 * Checks the format rules of the form: a place has a Russian name, a name in another language only when it is given;
 * its coordinates are a latitude from −90 to 90 and a longitude from −180 to 180, both or none.
 */
export function validatePlaceForm(values: PlaceFormValues): PlaceFormResult {
    const names: Partial<Record<UiLanguage, string>> = {};
    for (const language of PLACE_NAME_LANGUAGES) {
        const name = values.names[language].trim();
        if (name !== '') {
            names[language] = name;
        }
    }
    const latitude = coordinate(values.latitude, 90);
    const longitude = coordinate(values.longitude, 180);
    const errors: {nameRequired?: boolean; latitude?: CoordinateProblem; longitude?: CoordinateProblem} = {};
    if (names.ru === undefined) {
        errors.nameRequired = true;
    }
    if (Number.isNaN(latitude)) {
        errors.latitude = 'latitude-range';
    }
    if (Number.isNaN(longitude)) {
        errors.longitude = 'longitude-range';
    }
    if (latitude === null && longitude !== null) {
        errors.latitude = 'pair';
    }
    if (longitude === null && latitude !== null) {
        errors.longitude = 'pair';
    }
    if (names.ru === undefined || Object.keys(errors).length > 0) {
        return {ok: false, errors};
    }
    return {
        ok: true,
        draft: {names: {...names, ru: names.ru}, countryId: values.country === '' ? null : values.country, latitude, longitude},
    };
}

/** The form values of an existing country or city, or of an empty form. */
export function toPlaceFormValues(place?: PlaceDetails): PlaceFormValues {
    const names = perUiLanguage((language) => place?.names[language] ?? '');
    return {
        names,
        latitude: place?.latitude === null || place === undefined ? '' : String(place.latitude),
        longitude: place?.longitude === null || place === undefined ? '' : String(place.longitude),
        country: place?.countryId ?? '',
    };
}
