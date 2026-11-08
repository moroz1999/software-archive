import {describe, expect, it} from 'vitest';
import {isAppHref} from './href';

describe(isAppHref, () => {
    it('accepts a path of this app', () => {
        expect(isAppHref('/software?page=2')).toBe(true);
    });

    it('rejects absolute and protocol-relative URLs', () => {
        expect(isAppHref('https://example.com')).toBe(false);
        expect(isAppHref('//example.com/software')).toBe(false);
    });
});
