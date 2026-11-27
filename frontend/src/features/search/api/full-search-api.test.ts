import {afterEach, describe, expect, it, vi} from 'vitest';
import {json, stubFetch} from '@/shared/lib/test';
import {fetchFullSearch} from './full-search-api';

const ELITE = '00000000-0000-0000-00fd-000000000001';
const ZED = '00000000-0000-0000-00fd-000000000011';
const SQUAD = '00000000-0000-0000-00fd-000000000021';

const PAGE = {size: 50, number: 1, totalElements: 104, totalPages: 3};
const FOUND = {
    sets: [
        {type: 'author', total: 52, items: [{id: ZED, name: 'Zed', realName: 'John Doe', kind: 'person'}]},
        {type: 'group', total: 1, items: [{id: SQUAD, name: 'ZS'}]},
        {type: 'software', total: 51, items: [{id: ELITE, title: 'Elite', releaseDate: '1987-05-01', screenshots: [], madeBy: [], rating: 0, votesAmount: 0}]},
    ],
    page: PAGE,
};

describe(fetchFullSearch, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('asks with the keyword, the kinds chosen, the page and the platform', async () => {
        const fetchMock = stubFetch(json(FOUND));

        await fetchFullSearch({keyword: 'zed squad', types: ['author', 'group'], page: 1}, 'bk', new AbortController().signal);
        await fetchFullSearch({keyword: 'z', types: [], page: 0}, null, new AbortController().signal);

        expect(fetchMock.mock.calls.map(([url]) => url)).toStrictEqual([
            '/api/search?q=zed%20squad&types=author&types=group&page=1&platform=bk',
            '/api/search?q=z&page=0',
        ]);
    });
});
