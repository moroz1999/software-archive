import {deleteResource, getJson, type JsonObject, mapResponse, postJson, postNoContent, putJson, readArray, readNullableString, readNumber, readObject, readString} from '@/shared/api';
import {SUPPORTED_UI_LANGUAGES, type UiLanguage} from '@/shared/lib/i18n';
import {type CityId, type CountryId, toCityId, toCountryId} from '../model/geo';
import type {GeoDuplicate, GeoDuplicates, GeographyCity, GeographyCountry, PlaceDetails, PlaceKind, PlaceNames} from '../model/geography';
import type {PlaceDraft} from '../model/place-form';

function placesPath(kind: PlaceKind): string {
    return kind === 'country' ? '/api/countries' : '/api/cities';
}

function placePath(kind: PlaceKind, id: CountryId | CityId): string {
    return `${placesPath(kind)}/${id}`;
}

/** A country is sent without a country; a city with its own, or none. */
function toBody(kind: PlaceKind, draft: PlaceDraft): unknown {
    const coordinates = {latitude: draft.latitude, longitude: draft.longitude};
    return kind === 'country' ? {names: draft.names, ...coordinates} : {names: draft.names, countryId: draft.countryId, ...coordinates};
}

function readNullableNumber(object: JsonObject, key: string): number | null {
    return object[key] === null || object[key] === undefined ? null : readNumber(object, key);
}

/** The names in the languages of the site; one in another language is left out, a missing Russian one is malformed. */
function parseNames(value: unknown): PlaceNames {
    const dto = readObject(value, 'names');
    const names: Partial<Record<UiLanguage, string>> = {};
    for (const language of SUPPORTED_UI_LANGUAGES) {
        if (dto[language] !== undefined) {
            names[language] = readString(dto, language);
        }
    }
    return {...names, ru: readString(dto, 'ru')};
}

function parseCity(item: unknown): GeographyCity {
    const dto = readObject(item, 'city');
    return {id: toCityId(readString(dto, 'id')), name: readString(dto, 'name'), places: readNumber(dto, 'places')};
}

function parseCountry(item: unknown): GeographyCountry {
    const dto = readObject(item, 'country');
    return {
        id: toCountryId(readString(dto, 'id')),
        name: readString(dto, 'name'),
        places: readNumber(dto, 'places'),
        cities: readArray(dto['cities'], 'cities').map(parseCity),
    };
}

/** Every country with its cities and how many places are in them: `GET /api/geography`. */
export async function fetchGeography(signal: AbortSignal): Promise<readonly GeographyCountry[]> {
    const json = await getJson('/api/geography', signal);
    return mapResponse(() => readArray(json, 'countries').map(parseCountry));
}

/** One country or city with its names in every language: `GET /api/countries/{id}`, `GET /api/cities/{id}`. */
export async function fetchPlace(kind: PlaceKind, id: CountryId | CityId, signal: AbortSignal): Promise<PlaceDetails> {
    const json = await getJson(placePath(kind, id), signal);
    return mapResponse(() => {
        const dto = readObject(json, kind);
        const countryId = kind === 'city' ? readNullableString(dto, 'countryId') : null;
        const raw = readString(dto, 'id');
        return {
            kind,
            id: kind === 'country' ? toCountryId(raw) : toCityId(raw),
            names: parseNames(dto['names']),
            countryId: countryId === null ? null : toCountryId(countryId),
            latitude: readNullableNumber(dto, 'latitude'),
            longitude: readNullableNumber(dto, 'longitude'),
        };
    });
}

/** Adds a country or a city and returns its id. */
export async function createPlace(kind: PlaceKind, draft: PlaceDraft): Promise<string> {
    const json = await postJson(placesPath(kind), toBody(kind, draft));
    return mapResponse(() => readString(readObject(json, 'created place'), 'id'));
}

export async function updatePlace(kind: PlaceKind, id: CountryId | CityId, draft: PlaceDraft): Promise<void> {
    await putJson(placePath(kind, id), toBody(kind, draft));
}

/** Deletes a country or a city nothing of the archive is in. */
export async function deletePlace(kind: PlaceKind, id: CountryId | CityId): Promise<void> {
    await deleteResource(placePath(kind, id));
}

/** The cities of no known country with how many places are in them: `GET /api/geography/cities-of-no-country`. */
export async function fetchCitiesOfNoCountry(signal: AbortSignal): Promise<readonly GeographyCity[]> {
    const json = await getJson('/api/geography/cities-of-no-country', signal);
    return mapResponse(() => readArray(json, 'cities').map(parseCity));
}

/** Puts a city in a country, keeping all else it has. */
export async function putCityInCountry(city: CityId, country: CountryId): Promise<void> {
    await putJson(`/api/cities/${city}/country`, {countryId: country});
}

function parseDuplicate(item: unknown): GeoDuplicate {
    const dto = readObject(item, 'duplicate');
    return {id: readString(dto, 'id'), name: readString(dto, 'name'), country: readNullableString(dto, 'country'), places: readNumber(dto, 'places')};
}

/** The countries and the cities that may be duplicates: `GET /api/geography/duplicates`. */
export async function fetchGeoDuplicates(signal: AbortSignal): Promise<GeoDuplicates> {
    const json = await getJson('/api/geography/duplicates', signal);
    return mapResponse(() => {
        const dto = readObject(json, 'duplicates');
        return {
            countries: readArray(dto['countries'], 'countries').map((group) => readArray(group, 'group').map(parseDuplicate)),
            cities: readArray(dto['cities'], 'cities').map((item) => {
                const group = readObject(item, 'group');
                return {country: readNullableString(group, 'country'), cities: readArray(group['cities'], 'cities').map(parseDuplicate)};
            }),
        };
    });
}

/** Merges a country or a city into another one, the one that stays. */
export async function mergePlace(kind: PlaceKind, id: CountryId | CityId, into: string): Promise<void> {
    await postNoContent(`${placePath(kind, id)}/merge`, {into});
}
