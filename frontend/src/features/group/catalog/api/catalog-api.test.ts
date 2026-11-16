import {afterEach, describe, expect, it, vi} from 'vitest';
import {SystemError} from '@/shared/api';
import {json, pageResponse, stubFetch} from '@/shared/lib/test';
import {parseGroupCatalogFilter} from '../model/catalog-filter';
import {toPlatformCode} from '@/entities/platforms';
import {fetchGroupCatalogPage} from './catalog-api';

const GROUP = '00000000-0000-0000-0005-000000000001';
const LABEL = '00000000-0000-0000-0004-000000000001';
const COUNTRY = '00000000-0000-0000-0002-000000000001';
const CITY = '00000000-0000-0000-0002-000000000101';

describe(fetchGroupCatalogPage, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('asks for a page of 100 entries without filters', async () => {
        const fetchMock = stubFetch(json(pageResponse([
            {groupId: null, labelId: LABEL, name: 'CRW', note: 'Crew Ltd'},
            {groupId: GROUP, labelId: null, name: 'Crew Ltd', note: null},
        ], {size: 100})));

        const page = await fetchGroupCatalogPage(parseGroupCatalogFilter(new URLSearchParams()), null, new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/groups?page=0&size=100');
        expect(page.items).toStrictEqual([
            {kind: 'label', labelId: LABEL, name: 'CRW', note: 'Crew Ltd'},
            {kind: 'group', groupId: GROUP, name: 'Crew Ltd', note: null},
        ]);
    });

    it('asks with the filters and the page', async () => {
        const fetchMock = stubFetch(json(pageResponse([])));

        await fetchGroupCatalogPage(parseGroupCatalogFilter(new URLSearchParams(`letter=%23&country=${COUNTRY}&city=${CITY}&page=2`)), null, new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/groups?page=1&size=100&letter=%23&country=${COUNTRY}&city=${CITY}`);
    });

    it('asks only for the entries of the chosen platform', async () => {
        const fetchMock = stubFetch(json(pageResponse([])));

        await fetchGroupCatalogPage(parseGroupCatalogFilter(new URLSearchParams('letter=A')), toPlatformCode('bk'), new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/groups?page=0&size=100&letter=A&platform=bk');
    });

    it('fails with a system error on a malformed answer', async () => {
        stubFetch(json(pageResponse([{groupId: 'nope', labelId: null, name: 'Crew', note: null}])));

        await expect(fetchGroupCatalogPage(parseGroupCatalogFilter(new URLSearchParams()), null, new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
    });
});
