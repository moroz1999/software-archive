import {describe, expect, it} from 'vitest';
import {AUTHOR_LETTERS, catalogHref, parseCatalogFilter, toCityId, toCountryId} from './catalog-filter';

const COUNTRY = '00000000-0000-0000-0002-000000000001';
const CITY = '00000000-0000-0000-0002-000000000101';

function parse(search: string) {
    return parseCatalogFilter(new URLSearchParams(search));
}

describe(parseCatalogFilter, () => {
    it('is the whole first page without filters', () => {
        expect(parse('')).toStrictEqual({letter: null, country: null, city: null, page: 0});
    });

    it('reads the letter in any case, Ё as Е', () => {
        expect(parse('letter=b').letter).toBe('B');
        expect(parse('letter=%D1%8F').letter).toBe('Я');
        expect(parse('letter=%D1%91').letter).toBe('Е');
        expect(parse('letter=%23').letter).toBe('#');
    });

    it('ignores a letter that cannot be one', () => {
        expect(parse('letter=AB').letter).toBeNull();
        expect(parse('letter=1').letter).toBeNull();
    });

    it('reads the country and the city', () => {
        expect(parse(`country=${COUNTRY}&city=${CITY}`)).toMatchObject({country: COUNTRY, city: CITY});
    });

    it('ignores ids that cannot be ones, and a city without its country', () => {
        expect(parse('country=42&city=43')).toMatchObject({country: null, city: null});
        expect(parse(`city=${CITY}`).city).toBeNull();
    });

    it('reads the page counted from 1 in the address', () => {
        expect(parse('page=3').page).toBe(2);
        expect(parse('page=0').page).toBe(0);
        expect(parse('page=x').page).toBe(0);
        expect(parse('page=2.5').page).toBe(0);
    });
});

describe(catalogHref, () => {
    it('leaves out what is not filtered and the first page', () => {
        expect(catalogHref({letter: null, country: null, city: null, page: 0})).toBe('/authors');
    });

    it('writes every filter and the page counted from 1', () => {
        expect(catalogHref({letter: parse('letter=%23').letter, country: toCountryId(COUNTRY), city: toCityId(CITY), page: 1}))
            .toBe(`/authors?letter=%23&country=${COUNTRY}&city=${CITY}&page=2`);
    });
});

describe('AUTHOR_LETTERS', () => {
    it('are A–Z, А–Я without Ё, and # for the rest', () => {
        expect(AUTHOR_LETTERS).toHaveLength(26 + 32 + 1);
        expect(AUTHOR_LETTERS.slice(0, 2)).toStrictEqual(['A', 'B']);
        expect(AUTHOR_LETTERS.slice(25, 28)).toStrictEqual(['Z', 'А', 'Б']);
        expect(AUTHOR_LETTERS).not.toContain('Ё');
        expect(AUTHOR_LETTERS.at(-1)).toBe('#');
    });
});
