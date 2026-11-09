import {afterEach, describe, expect, it, vi} from 'vitest';
import {createQueryClient} from './query-client';
import {PRELOAD_WINDOW_MS, preloadView} from './preload-view';

const never = () => new Promise<string>(() => undefined);

describe(preloadView, () => {
    afterEach(() => {
        vi.useRealTimers();
    });

    it('finishes as soon as all queries settle, failed queries included', async () => {
        const client = createQueryClient();
        const settled = vi.fn<() => void>();

        await preloadView([
            client.query({queryKey: ['ok'], queryFn: () => Promise.resolve('data')}),
            client.query({queryKey: ['failed'], queryFn: () => Promise.reject(new Error('boom'))}),
        ]).then(settled);

        expect(settled).toHaveBeenCalledExactlyOnceWith(null);
    });

    it('stops waiting when the preload window is over', async () => {
        vi.useFakeTimers();
        const client = createQueryClient();
        const settled = vi.fn<() => void>();
        void preloadView([client.query({queryKey: ['slow'], queryFn: never})]).then(settled);

        await vi.advanceTimersByTimeAsync(PRELOAD_WINDOW_MS - 1);

        expect(settled).not.toHaveBeenCalled();

        await vi.advanceTimersByTimeAsync(1);

        expect(settled).toHaveBeenCalledExactlyOnceWith(null);
    });

    it('waits 300 ms at most', () => {
        expect(PRELOAD_WINDOW_MS).toBe(300);
    });
});
