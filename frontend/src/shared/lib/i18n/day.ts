/** The day of a moment in words of the interface language ("4 March 2026", "4 марта 2026 г."). */
export function dayText(moment: string, language: string): string {
    const locale = language.toLowerCase().startsWith('ru') ? 'ru-RU' : 'en-GB';
    return new Intl.DateTimeFormat(locale, {day: 'numeric', month: 'long', year: 'numeric', timeZone: 'UTC'}).format(new Date(moment));
}
