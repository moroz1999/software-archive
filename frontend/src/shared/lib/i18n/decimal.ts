/** A number as the interface language writes it, to two decimals at most: "4.08", "4,08". */
export function decimalText(value: number, language: string): string {
    const locale = language.toLowerCase().startsWith('ru') ? 'ru-RU' : 'en-GB';
    return new Intl.NumberFormat(locale, {maximumFractionDigits: 2}).format(value);
}
