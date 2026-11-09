import {describe, expect, it} from 'vitest';
import {parseCityId, parseCountryId, toCityId, toCountryId} from './geo';

const ID = '00000000-0000-0000-0002-000000000001';

describe(toCountryId, () => {
    it('accepts a uuid and refuses anything else', () => {
        expect(toCountryId(ID)).toBe(ID);
        expect(() => toCountryId('42')).toThrow(TypeError);
    });
});

describe(toCityId, () => {
    it('accepts a uuid and refuses anything else', () => {
        expect(toCityId(ID)).toBe(ID);
        expect(() => toCityId('42')).toThrow(TypeError);
    });
});

describe(parseCountryId, () => {
    it('reads an id from outside, null when it cannot be one', () => {
        expect(parseCountryId(ID)).toBe(ID);
        expect(parseCountryId('42')).toBeNull();
        expect(parseCountryId(null)).toBeNull();
    });
});

describe(parseCityId, () => {
    it('reads an id from outside, null when it cannot be one', () => {
        expect(parseCityId(ID)).toBe(ID);
        expect(parseCityId('42')).toBeNull();
        expect(parseCityId(null)).toBeNull();
    });
});
