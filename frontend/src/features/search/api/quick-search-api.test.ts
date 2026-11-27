import {afterEach, describe, expect, it, vi} from 'vitest';
import {json, stubFetch} from '@/shared/lib/test';
import {fetchQuickSearch} from './quick-search-api';

const ELITE = '00000000-0000-0000-00fd-000000000001';
const ZED = '00000000-0000-0000-00fd-000000000011';
const CARL = '00000000-0000-0000-00fd-000000000012';
const SQUAD = '00000000-0000-0000-00fd-000000000021';

const FOUND = {
    software: {items: [{id: ELITE, title: 'Elite', releaseDate: '1987-05-01', screenshots: [], madeBy: [], rating: 0, votesAmount: 0}], total: 12},
    authors: {items: [{id: ZED, name: 'Zed', realName: 'John Doe', kind: 'person'}, {id: CARL, name: 'Carl', realName: null, kind: 'label'}], total: 2},
    groups: {items: [{id: SQUAD, name: 'ZS'}], total: 1},
};

describe(fetchQuickSearch, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('asks with the keyword, and the platform when one is chosen', async () => {
        const fetchMock = stubFetch(json(FOUND));

        await fetchQuickSearch('zed squad', null, new AbortController().signal);
        await fetchQuickSearch('zed', 'bk', new AbortController().signal);

        expect(fetchMock.mock.calls.map(([url]) => url)).toStrictEqual([
            '/api/search/quick?q=zed%20squad',
            '/api/search/quick?q=zed&platform=bk',
        ]);
    });
});
