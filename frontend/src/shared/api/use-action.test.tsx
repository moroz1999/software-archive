import {afterEach, describe, expect, it, vi} from 'vitest';
import {act, renderHook, waitFor} from '@testing-library/react';
import {BusinessError, SystemError} from './errors';
import {subscribeSystemErrors, type SystemErrorReport} from './system-errors';
import {useAction} from './use-action';

describe(useAction, () => {
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

    it('is idle until run, running while the action works, and hands the result on', async () => {
        let finish: (value: string) => void = () => undefined;
        const action = vi.fn<(input: string) => Promise<string>>(() => new Promise((resolve) => {
            finish = resolve;
        }));
        const onSuccess = vi.fn<(result: string) => void>();
        const {result} = renderHook(() => useAction(action, onSuccess));

        expect(result.current.state).toStrictEqual({status: 'idle'});

        act(() => {
            result.current.run('input');
        });

        expect(result.current.state).toStrictEqual({status: 'running'});
        expect(action).toHaveBeenCalledExactlyOnceWith('input');

        act(() => {
            finish('done');
        });

        await waitFor(() => {
            expect(result.current.state).toStrictEqual({status: 'idle'});
        });

        expect(onSuccess).toHaveBeenCalledExactlyOnceWith('done');
    });

    it('returns a business error to the caller', async () => {
        const error = new BusinessError(404, 'software.not-found', []);
        const onSuccess = vi.fn<(result: string) => void>();
        const {result} = renderHook(() => useAction(() => Promise.reject(error), onSuccess));

        act(() => {
            result.current.run();
        });

        await waitFor(() => {
            expect(result.current.state).toStrictEqual({status: 'business-error', error});
        });

        expect(onSuccess).not.toHaveBeenCalled();
    });

    it('reports a system error with a retry that repeats the action with the same input', async () => {
        const reports = captureSystemErrors();
        const action = vi.fn<(input: string) => Promise<string>>()
            .mockRejectedValueOnce(new SystemError('server', 'HTTP 500'))
            .mockResolvedValueOnce('done');
        const onSuccess = vi.fn<(result: string) => void>();
        const {result} = renderHook(() => useAction(action, onSuccess));

        act(() => {
            result.current.run('input');
        });

        await waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(result.current.state).toStrictEqual({status: 'idle'});

        act(() => {
            reports[0]?.retry?.();
        });

        await waitFor(() => {
            expect(onSuccess).toHaveBeenCalledExactlyOnceWith('done');
        });

        expect(action).toHaveBeenNthCalledWith(2, 'input');
    });
});
