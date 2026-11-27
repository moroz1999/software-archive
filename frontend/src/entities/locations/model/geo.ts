import type {components} from '@/shared/api';
import {isUuid} from '@/shared/lib/uuid';

export type Country = components['schemas']['CountryDto'];
export type City = components['schemas']['CityDto'];

/** Where someone or something is located: a country, a city of it, or both; one location can be the primary one. */
export interface Location {
    readonly country: string | null;
    readonly city: string | null;
    readonly primary: boolean;
}

/** A country id in a raw value from outside (a URL), or null when it cannot be one. */
export function parseCountryId(raw: string | null): string | null {
    return raw !== null && isUuid(raw) ? raw : null;
}

/** A city id in a raw value from outside (a URL), or null when it cannot be one. */
export function parseCityId(raw: string | null): string | null {
    return raw !== null && isUuid(raw) ? raw : null;
}
