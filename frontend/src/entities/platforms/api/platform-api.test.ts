import {afterEach, describe, expect, it, vi} from 'vitest';
import {json, stubFetch} from '@/shared/lib/test';
import {fetchPlatforms} from './platform-api';

describe(fetchPlatforms, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('reads the platforms', async () => {
        const fetchMock = stubFetch(json([{code: 'agat', name: 'Agat'}, {code: 'bk', name: 'BK'}]));

        await expect(fetchPlatforms(new AbortController().signal)).resolves.toStrictEqual([
            {code: 'agat', name: 'Agat'},
            {code: 'bk', name: 'BK'},
        ]);
        expect(fetchMock.mock.calls[0]?.[0]).toBe('/api/platforms');
    });
});
