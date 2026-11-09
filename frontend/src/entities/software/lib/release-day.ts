import type {ReleaseDate} from '../model/software';

const DAY = new Intl.DateTimeFormat('en-GB', {day: 'numeric', month: 'long', year: 'numeric', timeZone: 'UTC'});

/** The release day in words, read in UTC so no time zone can shift it to a neighbouring day. */
export function releaseDayText(date: ReleaseDate): string {
    return DAY.format(new Date(`${date}T00:00:00Z`));
}
