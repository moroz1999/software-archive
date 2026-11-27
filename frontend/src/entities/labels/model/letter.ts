

/** Letters following one another in Unicode, from the first one on. */
function lettersFrom(first: string, count: number): string[] {
    const start = first.charCodeAt(0);
    return Array.from({length: count}, (_, index) => String.fromCharCode(start + index));
}

export const LATIN_LABEL_LETTERS: readonly string[] = lettersFrom('A', 26);

/** Ё lies outside the А–Я block and counts as Е. */
export const CYRILLIC_LABEL_LETTERS: readonly string[] = lettersFrom('А', 32);

export const REST_LABEL_LETTER = '#';

/** A–Z, then А–Я, then the rest. */
export const LABEL_LETTERS: readonly string[] = [...LATIN_LABEL_LETTERS, ...CYRILLIC_LABEL_LETTERS, REST_LABEL_LETTER];

/** A letter in a raw value from outside (a URL), in any case; null when it cannot be one. */
export function parseLabelLetter(raw: string | null): string | null {
    if (raw === null) {
        return null;
    }
    const upper = raw.toUpperCase();
    const letter = upper === 'Ё' ? 'Е' : upper;
    return LABEL_LETTERS.find((candidate) => candidate === letter) ?? null;
}
