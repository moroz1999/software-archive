import type {Location} from '../model/geo';

/** A location as visitors read it: "City, Country", or the one of them that is known. */
export function locationText(location: Location): string {
    return [location.city, location.country].filter((part) => part !== null).join(', ');
}

/** The primary location leads; the others keep their order. */
export function primaryFirst<T extends Location>(locations: readonly T[]): readonly T[] {
    return [...locations.filter((location) => location.primary), ...locations.filter((location) => !location.primary)];
}
