import {afterEach, describe, expect, it, vi} from 'vitest';
import type {components} from '@/shared/api';
import {json, pageResponse, stubFetch} from '@/shared/lib/test';
import {fetchLatestSoftware} from './fetch-software-page';

const ALPHA = {
    id: '00000000-0000-0000-0001-000000000001',
    title: 'Fixture Software Alpha',
    releaseDate: '1985-03-02',
    screenshots: [],
    madeBy: [],
    rating: 0,
    votesAmount: 0,
} satisfies components['schemas']['SoftwareListItemDto'];

describe(fetchLatestSoftware, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('requests the latest software of every platform', async () => {
        const fetchMock = stubFetch(json(pageResponse([ALPHA])));

        await fetchLatestSoftware(null, new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/software/latest');
    });

    it('requests only the latest software of the given platform', async () => {
        const fetchMock = stubFetch(json(pageResponse([ALPHA])));

        await fetchLatestSoftware('msx', new AbortController().signal);

        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/software/latest?platform=msx');
    });
});
