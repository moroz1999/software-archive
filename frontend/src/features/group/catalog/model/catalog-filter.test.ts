import {describe, expect, it} from 'vitest';
import {groupCatalogHref, parseGroupCatalogFilter} from './catalog-filter';

const COUNTRY = '00000000-0000-0000-0002-000000000001';
const CITY = '00000000-0000-0000-0002-000000000101';

function parse(search: string) {
    return parseGroupCatalogFilter(new URLSearchParams(search));
}

describe(parseGroupCatalogFilter, () => {
    it('reads the letter in any case, Ё as Е, the place and the page counted from 1', () => {
        expect(parse(`letter=%D1%91&country=${COUNTRY}&city=${CITY}&page=3`)).toStrictEqual({letter: 'Е', country: COUNTRY, city: CITY, page: 2});
    });

    it('leaves out what cannot be a filter, and a city without its country', () => {
        expect(parse('letter=AB&country=42&page=x')).toStrictEqual({letter: null, country: null, city: null, page: 0});
        expect(parse(`city=${CITY}`).city).toBeNull();
    });
});

describe(groupCatalogHref, () => {
    it('leaves out what is not filtered and the first page', () => {
        expect(groupCatalogHref({letter: null, country: null, city: null, page: 0})).toBe('/groups');
    });

    it('writes every filter and the page counted from 1', () => {
        expect(groupCatalogHref(parse(`letter=%23&country=${COUNTRY}&city=${CITY}&page=2`)))
            .toBe(`/groups?letter=%23&country=${COUNTRY}&city=${CITY}&page=2`);
    });
});
