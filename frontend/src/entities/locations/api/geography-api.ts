import {api, apiError, type components} from '@/shared/api';
import {SUPPORTED_UI_LANGUAGES, type UiLanguage} from '@/shared/lib/i18n';
import type {GeoDuplicates, GeographyCity, GeographyCountry, PlaceDetails, PlaceKind, PlaceNames} from '../model/geography';
import type {PlaceDraft} from '../model/place-form';

/** A country is sent without a country; a city with its own, or none. */
function toBody(kind: PlaceKind, draft: PlaceDraft): components['schemas']['PlaceRequestDto'] {
    return {names: draft.names, countryId: kind === 'country' ? null : draft.countryId, latitude: draft.latitude, longitude: draft.longitude};
}

/** The names in the languages of the site; one in another language is left out, a place without a Russian name is broken. */
function toNames(dto: Readonly<Record<string, string>>): PlaceNames {
    const names: Partial<Record<UiLanguage, string>> = {};
    for (const language of SUPPORTED_UI_LANGUAGES) {
        const name = dto[language];
        if (name !== undefined) {
            names[language] = name;
        }
    }
    const {ru} = dto;
    if (ru === undefined) {
        throw new TypeError('A place has a Russian name.');
    }
    return {...names, ru};
}

/** Every country with its cities and how many places are in them: `GET /api/geography`. */
export async function fetchGeography(signal: AbortSignal): Promise<readonly GeographyCountry[]> {
    const {data, error, response} = await api.GET('/api/geography', {signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** One country or city with its names in every language: `GET /api/countries/{id}`, `GET /api/cities/{id}`. */
export async function fetchPlace(kind: PlaceKind, id: string, signal: AbortSignal): Promise<PlaceDetails> {
    if (kind === 'country') {
        const {data, error, response} = await api.GET('/api/countries/{id}', {params: {path: {id}}, signal});
        if (error !== undefined) {
            throw apiError(response, error);
        }
        return {...data, kind, names: toNames(data.names), countryId: null};
    }
    const {data, error, response} = await api.GET('/api/cities/{id}', {params: {path: {id}}, signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return {...data, kind, names: toNames(data.names)};
}

/** Adds a country or a city and returns its id. */
export async function createPlace(kind: PlaceKind, draft: PlaceDraft): Promise<string> {
    const {data, error, response} = await api.POST(kind === 'country' ? '/api/countries' : '/api/cities', {body: toBody(kind, draft)});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data.id;
}

export async function updatePlace(kind: PlaceKind, id: string, draft: PlaceDraft): Promise<void> {
    const {error, response} = await api.PUT(kind === 'country' ? '/api/countries/{id}' : '/api/cities/{id}', {params: {path: {id}}, body: toBody(kind, draft)});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

/** Deletes a country or a city nothing of the archive is in. */
export async function deletePlace(kind: PlaceKind, id: string): Promise<void> {
    const {error, response} = await api.DELETE(kind === 'country' ? '/api/countries/{id}' : '/api/cities/{id}', {params: {path: {id}}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

/** The cities of no known country with how many places are in them: `GET /api/geography/cities-of-no-country`. */
export async function fetchCitiesOfNoCountry(signal: AbortSignal): Promise<readonly GeographyCity[]> {
    const {data, error, response} = await api.GET('/api/geography/cities-of-no-country', {signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** Puts a city in a country, keeping all else it has. */
export async function putCityInCountry(city: string, country: string): Promise<void> {
    const {error, response} = await api.PUT('/api/cities/{id}/country', {params: {path: {id: city}}, body: {countryId: country}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}

/** Turns a city of no known country into a country of its names and coordinates; the city goes. Returns the country's id. */
export async function turnCityIntoCountry(city: string): Promise<string> {
    const {data, error, response} = await api.POST('/api/cities/{id}/to-country', {params: {path: {id: city}}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data.id;
}

/** The countries and the cities that may be duplicates: `GET /api/geography/duplicates`. */
export async function fetchGeoDuplicates(signal: AbortSignal): Promise<GeoDuplicates> {
    const {data, error, response} = await api.GET('/api/geography/duplicates', {signal});
    if (error !== undefined) {
        throw apiError(response, error);
    }
    return data;
}

/** Merges a country or a city into another one, the one that stays. */
export async function mergePlace(kind: PlaceKind, id: string, into: string): Promise<void> {
    const {error, response} = await api.POST(kind === 'country' ? '/api/countries/{id}/merge' : '/api/cities/{id}/merge', {params: {path: {id}}, body: {into}});
    if (error !== undefined) {
        throw apiError(response, error);
    }
}
