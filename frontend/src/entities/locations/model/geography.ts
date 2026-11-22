import type {UiLanguage} from '@/shared/lib/i18n';
import {NotFoundError} from '@/shared/api';
import type {CityId, CountryId} from './geo';
import {parseCityId, parseCountryId} from './geo';

/** What is kept in the management: a country or a city. */
export type PlaceKind = 'country' | 'city';

/** The names of a country or a city in the languages of the site; the Russian one is always there. */
export type PlaceNames = Readonly<Partial<Record<UiLanguage, string>>> & {readonly ru: string};

/** A city of a country in the tree, with how many places of people, groups and labels are in it. */
export interface GeographyCity {
    readonly id: CityId;
    readonly name: string;
    readonly places: number;
}

/** A country in the tree, with how many places are in it, in its cities too, and its cities. */
export interface GeographyCountry {
    readonly id: CountryId;
    readonly name: string;
    readonly places: number;
    readonly cities: readonly GeographyCity[];
}

/** A country or a city as its form edits it; a country has no country, a city may have none while it is not known. */
export interface PlaceDetails {
    readonly kind: PlaceKind;
    readonly id: CountryId | CityId;
    readonly names: PlaceNames;
    readonly countryId: CountryId | null;
    readonly latitude: number | null;
    readonly longitude: number | null;
}

/** The name visitors see: in the language given, else the Russian one. */
export function placeName(names: PlaceNames, language: UiLanguage): string {
    return names[language] ?? names.ru;
}

/** The id of the place of the kind in a route parameter; one that cannot be an id is a page that does not exist. */
export function requirePlaceId(kind: PlaceKind, raw: string | undefined): CountryId | CityId {
    const id = kind === 'country' ? parseCountryId(raw ?? null) : parseCityId(raw ?? null);
    if (id === null) {
        throw new NotFoundError(kind === 'country' ? 'geo.country-not-found' : 'geo.city-not-found');
    }
    return id;
}

/** A country or a city that may be a duplicate, with a city's country, null for none, and how many places are in it. */
export interface GeoDuplicate {
    readonly id: string;
    readonly name: string;
    readonly country: string | null;
    readonly places: number;
}

/** The countries, and the cities of each country or of none, that may be one; each group of what may be one. */
export interface GeoDuplicates {
    readonly countries: readonly (readonly GeoDuplicate[])[];
    readonly cities: readonly {readonly country: string | null; readonly cities: readonly GeoDuplicate[]}[];
}
