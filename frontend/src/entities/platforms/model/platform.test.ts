import {describe, expect, it} from 'vitest';
import {parsePlatformCode, toPlatformCode} from './platform';

describe(toPlatformCode, () => {
    it('takes a code', () => {
        expect(toPlatformCode('vector')).toBe('vector');
    });

    it.each(['', 'Vector', 'zx spectrum', 'bk!'])('refuses %j', (raw) => {
        expect(() => toPlatformCode(raw)).toThrow(TypeError);
    });
});

describe(parsePlatformCode, () => {
    it('reads a code', () => {
        expect(parsePlatformCode('specialist')).toBe('specialist');
    });

    it.each([null, '', 'BK!'])('reads nothing from %j', (raw) => {
        expect(parsePlatformCode(raw)).toBeNull();
    });
});
