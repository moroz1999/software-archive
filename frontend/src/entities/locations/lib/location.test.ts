import {describe, expect, it} from 'vitest';
import {locationText, primaryFirst} from './location';

describe(locationText, () => {
    it('names the city with its country, or what is known', () => {
        expect(locationText({country: 'Finland', city: 'Helsinki', primary: true})).toBe('Helsinki, Finland');
        expect(locationText({country: 'Norway', city: null, primary: false})).toBe('Norway');
    });
});

describe(primaryFirst, () => {
    it('puts the primary location first and keeps the order of the others', () => {
        const norway = {country: 'Norway', city: null, primary: false};
        const sweden = {country: 'Sweden', city: null, primary: false};
        const finland = {country: 'Finland', city: null, primary: true};

        expect(primaryFirst([norway, finland, sweden])).toStrictEqual([finland, norway, sweden]);
    });
});
