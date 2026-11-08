import {afterEach, describe, expect, it, vi} from 'vitest';
import {PRELOAD_WINDOW_MS, preloadView, viewRequest, viewRequestFamily} from './preload';

describe(preloadView, () => {
    afterEach(() => {
        vi.useRealTimers();
    });

    it('starts every request of the view at once', () => {
        const first = vi.fn<(signal: AbortSignal) => Promise<string>>(() => new Promise(() => undefined));
        const second = vi.fn<(signal: AbortSignal) => Promise<string>>(() => new Promise(() => undefined));

        void preloadView([viewRequest(first), viewRequest(second)]);

        expect(first).toHaveBeenCalledExactlyOnceWith(expect.any(AbortSignal));
        expect(second).toHaveBeenCalledExactlyOnceWith(expect.any(AbortSignal));
    });

    it('finishes as soon as all requests settle, failed ones included', async () => {
        const settled = vi.fn<() => void>();

        await preloadView([viewRequest(() => Promise.resolve('data')), viewRequest(() => Promise.reject(new Error('boom')))]).then(settled);

        expect(settled).toHaveBeenCalledExactlyOnceWith(null);
    });

    it('stops waiting when the preload window is over', async () => {
        vi.useFakeTimers();
        const settled = vi.fn<() => void>();
        void preloadView([viewRequest(() => new Promise(() => undefined))]).then(settled);

        await vi.advanceTimersByTimeAsync(PRELOAD_WINDOW_MS - 1);

        expect(settled).not.toHaveBeenCalled();

        await vi.advanceTimersByTimeAsync(1);

        expect(settled).toHaveBeenCalledExactlyOnceWith(null);
    });

    it('waits 300 ms at most', () => {
        expect(PRELOAD_WINDOW_MS).toBe(300);
    });
});

describe(viewRequest, () => {
    it('hands the preloaded call to the block that shows it', async () => {
        const request = viewRequest(() => Promise.resolve('data'));

        await preloadView([request]);

        expect(request.take()?.settled).toStrictEqual({ok: true, value: 'data'});
    });

    it('has nothing to hand over when it was not preloaded', () => {
        expect(viewRequest(() => Promise.resolve('data')).take()).toBeUndefined();
    });

    it('hands a preloaded call over only right after the preload', async () => {
        vi.useFakeTimers();
        const request = viewRequest(() => Promise.resolve('data'));
        await preloadView([request]);

        expect(request.take()).toBeDefined();

        await vi.runAllTimersAsync();

        expect(request.take()).toBeUndefined();

        vi.useRealTimers();
    });
});

describe(viewRequestFamily, () => {
    it('keeps one request per key, loading by that key', async () => {
        const load = vi.fn<(key: string, signal: AbortSignal) => Promise<string>>((key) => Promise.resolve(`data of ${key}`));
        const family = viewRequestFamily(load);

        expect(family.for('a')).toBe(family.for('a'));
        expect(family.for('a')).not.toBe(family.for('b'));

        await preloadView([family.for('b')]);

        expect(load).toHaveBeenCalledExactlyOnceWith('b', expect.any(AbortSignal));
        expect(family.for('b').take()?.settled).toStrictEqual({ok: true, value: 'data of b'});
    });
});
