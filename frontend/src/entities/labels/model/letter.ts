import {type Brand} from '@/shared/lib/brand';

/** A letter of the catalogs of names: A–Z, А–Я (Ё counts as Е) or `#` for the rest (docs/domain/authors.md). */
export type LabelLetter = Brand<string, 'LabelLetter'>;

const REST_LETTER = '#';

/** Letters following one another in Unicode, from the first one on. */
function lettersFrom(first: string, count: number): string[] {
    const start = first.charCodeAt(0);
    return Array.from({length: count}, (_, index) => String.fromCharCode(start + index));
}

/** A–Z, then А–Я: Ё lies outside the А–Я block and counts as Е. */
export const LABEL_LETTERS: readonly LabelLetter[] = [...lettersFrom('A', 26), ...lettersFrom('А', 32), REST_LETTER]
    .map((letter) => letter as LabelLetter);

/** A letter in a raw value from outside (a URL), in any case; null when it cannot be one. */
export function parseLabelLetter(raw: string | null): LabelLetter | null {
    if (raw === null) {
        return null;
    }
    const upper = raw.toUpperCase();
    const letter = upper === 'Ё' ? 'Е' : upper;
    return LABEL_LETTERS.find((candidate) => candidate === letter) ?? null;
}
