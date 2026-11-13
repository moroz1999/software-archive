import {describe, expect, it} from 'vitest';
import {parseUserListFilter, userListHref} from './user-list-filter';

describe(parseUserListFilter, () => {
    it('reads the search and the page from the address, pages counting from 1 there', () => {
        expect(parseUserListFilter(new URLSearchParams('query=zed&page=3'))).toStrictEqual({query: 'zed', page: 2});
    });

    it('takes no search for an empty one and the first page for one that cannot be a page', () => {
        expect(parseUserListFilter(new URLSearchParams('query=%20%20&page=x'))).toStrictEqual({query: null, page: 0});
        expect(parseUserListFilter(new URLSearchParams())).toStrictEqual({query: null, page: 0});
    });

    it('reads the search without surrounding spaces', () => {
        expect(parseUserListFilter(new URLSearchParams('query=%20zed%20'))).toStrictEqual({query: 'zed', page: 0});
    });
});

describe(userListHref, () => {
    it('writes the search and the page into the address, leaving out what is not set', () => {
        expect(userListHref({query: null, page: 0})).toBe('/management/users');
        expect(userListHref({query: 'zed', page: 0})).toBe('/management/users?query=zed');
        expect(userListHref({query: 'zed', page: 1})).toBe('/management/users?query=zed&page=2');
    });
});
