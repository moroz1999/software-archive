import {describe, expect, it} from 'vitest';
import {TITLE_MAX_LENGTH, titleProblem} from './title';

describe(titleProblem, () => {
    it('finds nothing wrong with a filled title', () => {
        expect(titleProblem('Elite')).toBeNull();
        expect(titleProblem('a'.repeat(TITLE_MAX_LENGTH))).toBeNull();
    });

    it('requires a title', () => {
        expect(titleProblem('  ')).toBe('required');
    });

    it('limits a title to 255 characters', () => {
        expect(TITLE_MAX_LENGTH).toBe(255);
        expect(titleProblem('a'.repeat(256))).toBe('too-long');
    });
});
