import {describe, expect, it} from 'vitest';
import {toReleaseDate} from '../model/software';
import {releaseDayText} from './release-day';

describe(releaseDayText, () => {
    it('names the day with the month in words', () => {
        expect(releaseDayText(toReleaseDate('1984-09-20'))).toBe('20 September 1984');
    });

    it('keeps the first day of a month, whatever the time zone', () => {
        expect(releaseDayText(toReleaseDate('1985-01-01'))).toBe('1 January 1985');
    });
});
