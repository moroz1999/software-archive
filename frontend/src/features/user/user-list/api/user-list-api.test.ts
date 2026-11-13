import {afterEach, describe, expect, it, vi} from 'vitest';
import {json, pageResponse, stubFetch} from '@/shared/lib/test';
import {fetchUserPage} from './user-list-api';

const ZED = '00000000-0000-0000-0009-000000000001';
const AXE = '00000000-0000-0000-0009-000000000002';

describe(fetchUserPage, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('reads a page of users, without groups this interface does not know', async () => {
        const fetchMock = stubFetch(json(pageResponse([
            {id: ZED, name: 'Zed', email: 'zed@example.com', confirmed: true, registeredAt: '2026-01-01T10:00:00Z', groups: ['admin', 'member', 'owner']},
            {id: AXE, name: 'Axe', email: 'axe@example.com', confirmed: false, registeredAt: '2026-02-01T10:00:00Z', groups: []},
        ], {size: 50, totalElements: 2})));

        const page = await fetchUserPage({query: null, page: 0}, new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/users?page=0&size=50');
        expect(page.items).toStrictEqual([
            {id: ZED, name: 'Zed', email: 'zed@example.com', confirmed: true, registeredAt: '2026-01-01T10:00:00Z', groups: ['admin', 'member']},
            {id: AXE, name: 'Axe', email: 'axe@example.com', confirmed: false, registeredAt: '2026-02-01T10:00:00Z', groups: []},
        ]);
        expect(page.totalElements).toBe(2);
    });

    it('asks for the users of the search on the page asked', async () => {
        const fetchMock = stubFetch(json(pageResponse([], {size: 50, number: 2})));

        await fetchUserPage({query: 'zed', page: 2}, new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/users?page=2&size=50&query=zed');
    });

    it('fails as a broken response for a body it cannot read', async () => {
        stubFetch(json(pageResponse([{id: 'zed', name: 'Zed'}])));

        await expect(fetchUserPage({query: null, page: 0}, new AbortController().signal))
            .rejects.toThrow(expect.objectContaining({name: 'SystemError', kind: 'response'}));
    });
});
