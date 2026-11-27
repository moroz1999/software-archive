import {describe, expect, it} from 'vitest';
import {NotFoundError} from '@/shared/api';
import {isReleaseDate, requireSoftwareId} from './software';

describe(isReleaseDate, () => {
    it('takes an ISO calendar date', () => {
        expect(isReleaseDate('1985-03-02')).toBe(true);
    });

    it('takes a year alone', () => {
        expect(isReleaseDate('1985')).toBe(true);
    });

    it.each(['1985-03-02T10:00:00Z', '85', '19850', '1985-09', '1985-13-01', '1985-02-30', '1985-3-2', '1987 / 2026'])('refuses %j, neither a year nor a day of it', (raw) => {
        expect(isReleaseDate(raw)).toBe(false);
    });
});

describe(requireSoftwareId, () => {
    it('takes the id from the URL', () => {
        expect(requireSoftwareId('00000000-0000-0000-0001-000000000001')).toBe('00000000-0000-0000-0001-000000000001');
    });

    it.each([undefined, '42'])('treats %s as a page that does not exist', (raw) => {
        expect(() => requireSoftwareId(raw)).toThrow(NotFoundError);
    });
});
