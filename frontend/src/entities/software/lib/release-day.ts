import type {ReleaseDate} from '../model/software';

/** The release day in words, read in UTC so no time zone can shift it to a neighbouring day. */
export function releaseDayText(date: ReleaseDate, language = 'en-GB'): string {
    const locale = language.toLowerCase().startsWith('ru') ? 'ru-RU' : 'en-GB';
    return new Intl.DateTimeFormat(locale, {day: 'numeric', month: 'long', year: 'numeric', timeZone: 'UTC'})
        .format(new Date(`${date}T00:00:00Z`));
}
