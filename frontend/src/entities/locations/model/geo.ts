import {type Brand} from '@/shared/lib/brand';
import {isUuid} from '@/shared/lib/uuid';

export type CountryId = Brand<string, 'CountryId'>;
export type CityId = Brand<string, 'CityId'>;

export interface Country {
    readonly id: CountryId;
    readonly name: string;
}

export interface City {
    readonly id: CityId;
    readonly name: string;
}

/** Where someone or something is located: a country, a city of it, or both; one location can be the primary one. */
export interface Location {
    readonly country: string | null;
    readonly city: string | null;
    readonly primary: boolean;
}

export function toCountryId(raw: string): CountryId {
    if (!isUuid(raw)) {
        throw new TypeError(`Invalid country id: ${raw}`);
    }
    return raw as CountryId;
}

export function toCityId(raw: string): CityId {
    if (!isUuid(raw)) {
        throw new TypeError(`Invalid city id: ${raw}`);
    }
    return raw as CityId;
}

/** A country id in a raw value from outside (a URL), or null when it cannot be one. */
export function parseCountryId(raw: string | null): CountryId | null {
    return raw !== null && isUuid(raw) ? toCountryId(raw) : null;
}

/** A city id in a raw value from outside (a URL), or null when it cannot be one. */
export function parseCityId(raw: string | null): CityId | null {
    return raw !== null && isUuid(raw) ? toCityId(raw) : null;
}
