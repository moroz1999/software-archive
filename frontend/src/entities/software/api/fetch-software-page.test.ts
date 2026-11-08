import {afterEach, describe, expect, it, vi} from 'vitest';
import {SystemError} from '../../../shared/api';
import {json, pageResponse, stubFetch} from '../../../shared/lib/test';
import {fetchSoftwarePage} from './fetch-software-page';

const ALPHA = {id: '00000000-0000-0000-0001-000000000001', title: 'Fixture Software Alpha', releaseDate: '1985-03-02'};
const BETA = {id: '00000000-0000-0000-0001-000000000002', title: 'Fixture Software Beta', releaseDate: null};

describe(fetchSoftwarePage, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('requests the page and maps it to software', async () => {
        const fetchMock = stubFetch(json(pageResponse([ALPHA, BETA])));

        const page = await fetchSoftwarePage(0, new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/software?page=0&size=20');
        expect(page.items).toStrictEqual([
            {id: ALPHA.id, title: ALPHA.title, releaseDate: '1985-03-02'},
            {id: BETA.id, title: BETA.title, releaseDate: null},
        ]);
        expect(page.totalElements).toBe(2);
    });

    it('fails with a system error when an item is malformed', async () => {
        stubFetch(json(pageResponse([{...ALPHA, id: 'not-a-uuid'}])));

        await expect(fetchSoftwarePage(0, new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
    });
});
