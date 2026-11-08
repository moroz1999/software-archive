import type {ReleaseDate} from '../model/software';

/** The year of the release day, read from the ISO date itself so no time zone can shift it. */
export function releaseYear(date: ReleaseDate): number {
    return Number(date.slice(0, 4));
}
