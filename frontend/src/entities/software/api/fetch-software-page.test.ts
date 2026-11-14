import {afterEach, describe, expect, it, vi} from 'vitest';
import {SystemError} from '@/shared/api';
import {json, pageResponse, stubFetch} from '@/shared/lib/test';
import {fetchLatestSoftware} from './fetch-software-page';

const ALPHA = {id: '00000000-0000-0000-0001-000000000001', title: 'Fixture Software Alpha', releaseDate: '1985-03-02'};

describe(fetchLatestSoftware, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('requests the latest software of every platform', async () => {
        const fetchMock = stubFetch(json(pageResponse([ALPHA])));

        await fetchLatestSoftware(null, new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/software/latest?size=12');
    });

    it('requests only the latest software of the given platform', async () => {
        const fetchMock = stubFetch(json(pageResponse([ALPHA])));

        await fetchLatestSoftware('msx', new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/software/latest?size=12&platform=msx');
    });

    it('reads the screenshots each card shows, none when missing', async () => {
        const shot = {id: '00000000-0000-0000-0055-000000000001', url: '/api/screenshots/00000000-0000-0000-0055-000000000001'};
        stubFetch(json(pageResponse([{...ALPHA, screenshots: [shot]}, ALPHA])));

        const page = await fetchLatestSoftware(null, new AbortController().signal);

        expect(page.items.map((software) => software.screenshots)).toStrictEqual([[shot], []]);
    });

    it('reads who made the software of each card, no one when missing', async () => {
        stubFetch(json(pageResponse([{...ALPHA, madeBy: ['Ultimate', 'Zed']}, ALPHA])));

        const page = await fetchLatestSoftware(null, new AbortController().signal);

        expect(page.items.map((software) => software.madeBy)).toStrictEqual([['Ultimate', 'Zed'], []]);
    });

    it('fails with a system error when an item is malformed', async () => {
        stubFetch(json(pageResponse([{...ALPHA, id: 'not-a-uuid'}])));

        await expect(fetchLatestSoftware(null, new AbortController().signal)).rejects.toBeInstanceOf(SystemError);
    });
});
