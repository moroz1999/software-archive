import {describe, expect, it} from 'vitest';
import {toReleaseDate} from '../model/software';
import {releaseDateText} from './release-date';

describe(releaseDateText, () => {
    it('writes a day as dd.mm.yyyy', () => {
        expect(releaseDateText(toReleaseDate('1984-09-20'))).toBe('20.09.1984');
        expect(releaseDateText(toReleaseDate('1985-01-01'))).toBe('01.01.1985');
    });

    it('writes a year alone as it is', () => {
        expect(releaseDateText(toReleaseDate('1987'))).toBe('1987');
    });
});
