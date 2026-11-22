import {describe, expect, it} from 'vitest';
import {toCategoryId} from '@/entities/categories';
import {NotFoundError} from '@/shared/api';
import {toCityId, toCountryId} from '@/entities/locations';
import {toPlatformCode} from '@/entities/platforms';
import {catalogHref, parseCatalogFilter} from './catalog-filter';

const GAMES = '00000000-0000-0000-00fd-0000000000c1';
const FINLAND = '00000000-0000-0000-00fd-0000000000f1';
const HELSINKI = '00000000-0000-0000-00fd-0000000000e1';
const NONE = {city: null, platforms: [], hardware: []};

function parse(search: string, category?: string) {
    return parseCatalogFilter(category, new URLSearchParams(search));
}

/** The filter of an address the catalog wrote. */
function readBack(href: string) {
    const url = new URL(href, 'http://archive');
    const category = /^\/software\/category\/([^/]+)$/u.exec(url.pathname)?.[1];
    return parseCatalogFilter(category, url.searchParams);
}

describe(parseCatalogFilter, () => {
    it('is the whole first page without a keyword or filters', () => {
        expect(parse('')).toStrictEqual({keyword: null, year: null, category: null, country: null, ...NONE, page: 0});
    });

    it('reads the category from the path, the keyword, the filters and the page counted from 1 from the query', () => {
        expect(parse(`q=elite&year=1987&country=${FINLAND}&page=3`, GAMES)).toStrictEqual({
            keyword: 'elite', year: 1987, category: GAMES, country: FINLAND, ...NONE, page: 2,
        });
    });

    it('reads the city with its country, the platforms and the hardware chosen', () => {
        expect(parse(`country=${FINLAND}&city=${HELSINKI}&platforms=bk,vector&hardware=ay8910,z80`)).toMatchObject({
            country: FINLAND, city: HELSINKI, platforms: ['bk', 'vector'], hardware: ['ay8910', 'z80'],
        });
    });

    it('takes no city without its country, and leaves out platforms and hardware that cannot be codes', () => {
        expect(parse(`city=${HELSINKI}`).city).toBeNull();
        expect(parse(`country=${FINLAND}&city=43`).city).toBeNull();
        expect(parse('platforms=bk,BK!,,vector&hardware=AY 8910,z80')).toMatchObject({platforms: ['bk', 'vector'], hardware: ['z80']});
    });

    it('takes a blank keyword for none and trims one', () => {
        expect(parse('q=%20%20').keyword).toBeNull();
        expect(parse('q=%20elite%20').keyword).toBe('elite');
    });

    it('takes no category from the query', () => {
        expect(parse(`category=${GAMES}`).category).toBeNull();
    });

    it('is no page for a category in the path that cannot be an id', () => {
        expect(() => parse('', 'games')).toThrow(NotFoundError);
    });

    it('ignores a year and a country that cannot be ones', () => {
        expect(parse('year=nineteen&country=43')).toMatchObject({year: null, country: null});
        expect(parse('year=19.5').year).toBeNull();
        expect(parse('year=-1').year).toBeNull();
    });
});

describe(catalogHref, () => {
    it('is the catalog itself without a keyword, filters or a page', () => {
        expect(catalogHref({keyword: null, year: null, category: null, country: null, ...NONE, page: 0})).toBe('/software');
    });

    it('writes what is set, the page counted from 1', () => {
        expect(catalogHref({
            keyword: 'tetris', year: 1989, category: toCategoryId(GAMES), country: toCountryId(FINLAND), ...NONE, page: 1,
        })).toBe(`/software/category/${GAMES}?q=tetris&year=1989&country=${FINLAND}&page=2`);
    });

    it('writes the city, the platforms and the hardware after the country', () => {
        expect(catalogHref({
            keyword: null, year: null, category: null, country: toCountryId(FINLAND), city: toCityId(HELSINKI),
            platforms: [toPlatformCode('bk'), toPlatformCode('vector')], hardware: ['ay8910', 'z80'], page: 0,
        })).toBe(`/software?country=${FINLAND}&city=${HELSINKI}&platforms=bk%2Cvector&hardware=ay8910%2Cz80`);
    });

    it('puts the category alone in the path', () => {
        expect(catalogHref({keyword: null, year: null, category: toCategoryId(GAMES), country: null, ...NONE, page: 0})).toBe(`/software/category/${GAMES}`);
    });

    it('is read back as the same filter', () => {
        const filter = {
            keyword: 'Элита 2', year: 1987, category: toCategoryId(GAMES), country: toCountryId(FINLAND), city: toCityId(HELSINKI),
            platforms: [toPlatformCode('bk')], hardware: ['ay8910'], page: 4,
        };

        expect(readBack(catalogHref(filter))).toStrictEqual(filter);
        expect(readBack(catalogHref({...filter, category: null}))).toStrictEqual({...filter, category: null});
    });
});
