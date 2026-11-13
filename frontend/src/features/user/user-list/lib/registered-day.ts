/** The day an account registered, in words of the interface language. */
export function registeredDayText(registeredAt: string, language: string): string {
    const locale = language.toLowerCase().startsWith('ru') ? 'ru-RU' : 'en-GB';
    return new Intl.DateTimeFormat(locale, {day: 'numeric', month: 'long', year: 'numeric', timeZone: 'UTC'}).format(new Date(registeredAt));
}
