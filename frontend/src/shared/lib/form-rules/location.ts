/** The rules of a location in a form (docs/domain/geo.md), the same for every owner of locations. */

/** A location as a form holds it: the ids of the chosen country and city, each empty while not chosen. */
export interface LocationValue {
    readonly country: string;
    readonly city: string;
    /** The name of a city of no known country: no country offers it, so the form keeps its name to show it. */
    readonly cityName?: string;
    readonly primary: boolean;
}


/** A location as a form sends it: the ids of the country and the city, each null when not given. */
export interface LocationDraft {
    readonly country: string | null;
    readonly city: string | null;
    readonly primary: boolean;
}

/** A location as the archive tells it, by the ids of its country and city and the name of the city. */
export interface StoredLocation {
    readonly countryId: string | null;
    readonly cityId: string | null;
    readonly city: string | null;
    readonly primary: boolean;
}

/**
 * What is wrong with a location of a form: it needs a country, unless it is a city of no known country, kept as it is
 * (docs/domain/geo.md).
 */
export function locationProblem(value: LocationValue): 'country-required' | null {
    return value.country === '' && value.city === '' ? 'country-required' : null;
}

export function toLocationDraft(value: LocationValue): LocationDraft {
    return {
        country: value.country === '' ? null : value.country,
        city: value.city === '' ? null : value.city,
        primary: value.primary,
    };
}

/** A stored location in a form; a city of no known country keeps its name, as no country offers it. */
export function toLocationValue(location: StoredLocation): LocationValue {
    const value = {country: location.countryId ?? '', city: location.cityId ?? '', primary: location.primary};
    return location.countryId === null && location.cityId !== null && location.city !== null ? {...value, cityName: location.city} : value;
}
