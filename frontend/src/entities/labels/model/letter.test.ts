import {describe, expect, it} from 'vitest';
import {LABEL_LETTERS, parseLabelLetter} from './letter';

describe('LABEL_LETTERS', () => {
    it('are A–Z, А–Я without Ё, and # for the rest', () => {
        expect(LABEL_LETTERS).toHaveLength(26 + 32 + 1);
        expect(LABEL_LETTERS.slice(0, 2)).toStrictEqual(['A', 'B']);
        expect(LABEL_LETTERS.slice(25, 28)).toStrictEqual(['Z', 'А', 'Б']);
        expect(LABEL_LETTERS).not.toContain('Ё');
        expect(LABEL_LETTERS.at(-1)).toBe('#');
    });
});

describe(parseLabelLetter, () => {
    it('reads the letter in any case, Ё as Е', () => {
        expect(parseLabelLetter('b')).toBe('B');
        expect(parseLabelLetter('я')).toBe('Я');
        expect(parseLabelLetter('ё')).toBe('Е');
        expect(parseLabelLetter('#')).toBe('#');
    });

    it('is null for anything that cannot be a letter', () => {
        expect(parseLabelLetter('AB')).toBeNull();
        expect(parseLabelLetter('1')).toBeNull();
        expect(parseLabelLetter(null)).toBeNull();
    });
});
