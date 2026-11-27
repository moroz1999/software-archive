import {describe, expect, it} from 'vitest';
import {releaseYear} from './release-year';

describe(releaseYear, () => {
    it('is the year of the calendar day', () => {
        expect(releaseYear('1985-03-02')).toBe(1985);
    });
});
