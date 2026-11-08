import {afterEach, describe, expect, it, vi} from 'vitest';
import {act, renderHook, waitFor} from '@testing-library/react';
import {useRequest} from './use-request';
import {preloadView, viewRequest} from './preload';
import {BusinessError, SystemError} from './errors';
import {subscribeSystemErrors, type SystemErrorReport} from './system-errors';

describe(useRequest, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    function captureSystemErrors(): SystemErrorReport[] {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        return reports;
    }

    it('starts loading and then holds the data', async () => {
        const {result} = renderHook(() => useRequest(() => Promise.resolve('data')));

        expect(result.current.state).toStrictEqual({status: 'loading'});

        await waitFor(() => {
            expect(result.current.state).toStrictEqual({status: 'success', data: 'data', reloading: false});
        });
    });

    it('exposes a business error to the caller and does not report it as a system error', async () => {
        const reports = captureSystemErrors();
        const error = new BusinessError(422, 'software.title-taken', []);

        const {result} = renderHook(() => useRequest(() => Promise.reject(error)));

        await waitFor(() => {
            expect(result.current.state).toStrictEqual({status: 'business-error', error});
        });

        expect(reports).toHaveLength(0);
    });

    it('reports a system error with a retry that repeats the request', async () => {
        const reports = captureSystemErrors();
        const load = vi.fn<() => Promise<string>>()
            .mockRejectedValueOnce(new SystemError('server', 'HTTP 500'))
            .mockResolvedValueOnce('data');

        const {result} = renderHook(() => useRequest(load));

        await waitFor(() => {
            expect(result.current.state).toStrictEqual({status: 'system-error'});
        });

        expect(reports).toHaveLength(1);

        act(() => {
            reports[0]?.retry?.();
        });

        await waitFor(() => {
            expect(result.current.state).toStrictEqual({status: 'success', data: 'data', reloading: false});
        });

        expect(load).toHaveBeenCalledTimes(2);
    });

    it('treats an unknown failure as a system error', async () => {
        const reports = captureSystemErrors();

        const {result} = renderHook(() => useRequest(() => Promise.reject(new Error('boom'))));

        await waitFor(() => {
            expect(result.current.state).toStrictEqual({status: 'system-error'});
        });

        expect(reports[0]?.error).toBeInstanceOf(SystemError);
    });

    it('waits until enabled before loading', async () => {
        const load = vi.fn<(signal: AbortSignal) => Promise<string>>(() => Promise.resolve('data'));

        const {result, rerender} = renderHook(({enabled}) => useRequest(load, {enabled}), {initialProps: {enabled: false}});

        expect(result.current.state).toStrictEqual({status: 'loading'});
        expect(load).not.toHaveBeenCalled();

        rerender({enabled: true});

        await waitFor(() => {
            expect(result.current.state).toStrictEqual({status: 'success', data: 'data', reloading: false});
        });
    });

    it('shows preloaded data at once without loading again', async () => {
        const load = vi.fn<(signal: AbortSignal) => Promise<string>>(() => Promise.resolve('data'));
        const request = viewRequest(load);
        await preloadView([request]);

        const {result} = renderHook(() => useRequest(request));

        expect(result.current.state).toStrictEqual({status: 'success', data: 'data', reloading: false});
        expect(load).toHaveBeenCalledExactlyOnceWith(expect.any(AbortSignal));
    });

    it('shows a preloaded business error at once', async () => {
        const error = new BusinessError(404, 'software.not-found', []);
        const request = viewRequest(() => Promise.reject(error));
        await preloadView([request]);

        const {result} = renderHook(() => useRequest(request));

        expect(result.current.state).toStrictEqual({status: 'business-error', error});
    });

    it('reports a preloaded system error', async () => {
        const reports = captureSystemErrors();
        const request = viewRequest(() => Promise.reject(new SystemError('network', 'offline')));
        await preloadView([request]);

        const {result} = renderHook(() => useRequest(request));

        await waitFor(() => {
            expect(result.current.state).toStrictEqual({status: 'system-error'});
        });

        expect(reports).toHaveLength(1);
    });

    it('waits for a preload still in flight instead of loading again', async () => {
        let resolve: (value: string) => void = () => undefined;
        const load = vi.fn<(signal: AbortSignal) => Promise<string>>(() => new Promise((done) => {
            resolve = done;
        }));
        const request = viewRequest(load);
        void preloadView([request], 0);

        const {result} = renderHook(() => useRequest(request));

        expect(result.current.state).toStrictEqual({status: 'loading'});

        act(() => {
            resolve('data');
        });

        await waitFor(() => {
            expect(result.current.state).toStrictEqual({status: 'success', data: 'data', reloading: false});
        });

        expect(load).toHaveBeenCalledExactlyOnceWith(expect.any(AbortSignal));
    });

    it('loads afresh on reload after a preload', async () => {
        const load = vi.fn<() => Promise<string>>()
            .mockResolvedValueOnce('first')
            .mockResolvedValueOnce('second');
        const request = viewRequest(load);
        await preloadView([request]);
        const {result} = renderHook(() => useRequest(request));

        act(() => {
            result.current.reload();
        });

        expect(result.current.state).toStrictEqual({status: 'success', data: 'first', reloading: true});

        await waitFor(() => {
            expect(result.current.state).toStrictEqual({status: 'success', data: 'second', reloading: false});
        });
    });
});
