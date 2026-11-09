import {afterEach, describe, expect, it, vi} from 'vitest';
import {SystemError} from '@/shared/api';
import {json, pageResponse, stubFetch} from '@/shared/lib/test';
import {parseGroupCatalogFilter} from '../model/catalog-filter';
import {fetchGroupCatalogPage} from './catalog-api';

const GROUP = '00000000-0000-0000-0005-000000000001';
const COUNTRY = '00000000-0000-0000-0002-000000000001';
const CITY = '00000000-0000-0000-0002-000000000101';

describe(fetchGroupCatalogPage, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('asks for a page of 100 entries without filters', async () => {
        const fetchMock = stubFetch(json(pageResponse([
            {groupId: GROUP, name: 'CRW', groupName: 'Crew Ltd'},
            {groupId: GROUP, name: 'Crew Ltd', groupName: null},
        ], {size: 100})));

        const page = await fetchGroupCatalogPage(parseGroupCatalogFilter(new URLSearchParams()), new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/groups?page=0&size=100');
        expect(page.items).toStrictEqual([
            {groupId: GROUP, name: 'CRW', groupName: 'Crew Ltd'},
            {groupId: GROUP, name: 'Crew Ltd', groupName: null},
        ]);
    });

    it('asks with the filters and the page', async () => {
        const fetchMock = stubFetch(json(pageResponse([])));

        await fetchGroupCatalogPage(parseGroupCatalogFilter(new URLSearchParams(`letter=%23&country=${COUNTRY}&city=${CITY}&page=2`)), new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe(`/api/groups?page=1&size=100&letter=%23&country=${COUNTRY}&city=${CITY}`);
    });

    it('fails with a system error on a malformed answer', async () => {
        stubFetch(json(pageResponse([{groupId: 'nope', name: 'Crew', groupName: null}])));

        await expect(fetchGroupCatalogPage(parseGroupCatalogFilter(new URLSearchParams()), new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
    });
});
