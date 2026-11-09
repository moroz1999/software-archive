import {describe, expect, it} from 'vitest';
import {pageParam, parsePageParam} from './page-param';

describe(parsePageParam, () => {
    it('reads the page counted from 1 in the address', () => {
        expect(parsePageParam('3')).toBe(2);
        expect(parsePageParam('1')).toBe(0);
    });

    it('is the first page for anything that cannot be one', () => {
        expect(parsePageParam(null)).toBe(0);
        expect(parsePageParam('0')).toBe(0);
        expect(parsePageParam('x')).toBe(0);
        expect(parsePageParam('2.5')).toBe(0);
    });
});

describe(pageParam, () => {
    it('writes the page counted from 1 and leaves out the first one', () => {
        expect(pageParam(1)).toBe('2');
        expect(pageParam(0)).toBeNull();
    });
});
