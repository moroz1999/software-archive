import {describe, expect, it} from 'vitest';
import {NotFoundError} from '@/shared/api';
import {requireSoftwareId, toReleaseDate, toSoftwareId, toSoftwareTitle} from './software';

describe(toSoftwareId, () => {
    it('accepts a UUID', () => {
        expect(toSoftwareId('00000000-0000-0000-0001-000000000001')).toBe('00000000-0000-0000-0001-000000000001');
    });

    it('rejects anything else', () => {
        expect(() => toSoftwareId('42')).toThrow(TypeError);
    });
});

describe(toSoftwareTitle, () => {
    it('rejects a blank title', () => {
        expect(() => toSoftwareTitle('  ')).toThrow(TypeError);
    });

    it('accepts 255 characters and rejects more', () => {
        expect(toSoftwareTitle('a'.repeat(255))).toHaveLength(255);
        expect(() => toSoftwareTitle('a'.repeat(256))).toThrow(TypeError);
    });
});

describe(toReleaseDate, () => {
    it('accepts an ISO calendar date', () => {
        expect(toReleaseDate('1985-03-02')).toBe('1985-03-02');
    });

    it('rejects a date with time', () => {
        expect(() => toReleaseDate('1985-03-02T10:00:00Z')).toThrow(TypeError);
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
