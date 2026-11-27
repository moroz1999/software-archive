import {isReleaseDate} from '../model/software';

const YEAR_TEXT = /^\d{4}$/u;
const DAY_TEXT = /^(\d{2})\.(\d{2})\.(\d{4})$/u;

/** The release date as people read and write it (docs/domain/software.md#release-dates): a year, or a day as dd.mm.yyyy. */
export function releaseDateText(date: string): string {
    const [year = '', month = '', day = ''] = date.split('-');
    return date.length === 4 ? date : `${day}.${month}.${year}`;
}

/** A known release date as a form field holds it; an unknown one, empty. */
export function releaseDateField(date: string | null | undefined): string {
    return date === null || date === undefined ? '' : releaseDateText(date);
}

/** The release date written as {@link releaseDateText} writes it; null for anything else, or a day the calendar lacks. */
export function parseReleaseDateText(text: string): string | null {
    if (YEAR_TEXT.test(text)) {
        return text;
    }
    const day = DAY_TEXT.exec(text);
    if (day === null) {
        return null;
    }
    const [, dd = '', mm = '', yyyy = ''] = day;
    const iso = `${yyyy}-${mm}-${dd}`;
    return isReleaseDate(iso) ? iso : null;
}
