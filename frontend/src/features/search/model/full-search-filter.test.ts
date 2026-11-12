import {describe, expect, it} from 'vitest';
import {fullSearchHref, parseFullSearchFilter} from './full-search-filter';

describe(parseFullSearchFilter, () => {
    it('reads the keyword, the kinds in their order and the page, counted from 0', () => {
        expect(parseFullSearchFilter(new URLSearchParams('q=%20zed%20&types=software,author&page=3'))).toStrictEqual({
            keyword: 'zed', types: ['author', 'software'], page: 2,
        });
    });

    it('takes every kind, or none known, for all of them', () => {
        expect(parseFullSearchFilter(new URLSearchParams('q=z&types=group,software,author')).types).toStrictEqual([]);
        expect(parseFullSearchFilter(new URLSearchParams('q=z&types=picture')).types).toStrictEqual([]);
    });

    it('takes a blank keyword for none and what cannot be a page for the first one', () => {
        expect(parseFullSearchFilter(new URLSearchParams('q=%20&page=x'))).toStrictEqual({keyword: null, types: [], page: 0});
    });
});

describe(fullSearchHref, () => {
    it('writes the keyword, the kinds chosen and the page after the first, counted from 1', () => {
        expect(fullSearchHref({keyword: 'zed squad', types: ['group', 'software'], page: 1})).toBe('/search?q=zed+squad&types=group%2Csoftware&page=2');
    });

    it('leaves out every kind and the first page', () => {
        expect(fullSearchHref({keyword: 'z', types: [], page: 0})).toBe('/search?q=z');
        expect(fullSearchHref({keyword: null, types: [], page: 0})).toBe('/search');
    });
});
