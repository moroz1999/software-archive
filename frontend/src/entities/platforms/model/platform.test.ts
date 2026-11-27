import {describe, expect, it} from 'vitest';
import {parsePlatformCode} from './platform';

describe(parsePlatformCode, () => {
    it('reads a code', () => {
        expect(parsePlatformCode('specialist')).toBe('specialist');
    });

    it.each([null, '', 'BK!'])('reads nothing from %j', (raw) => {
        expect(parsePlatformCode(raw)).toBeNull();
    });
});
