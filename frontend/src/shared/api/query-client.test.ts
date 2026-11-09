import {afterEach, describe, expect, it, vi} from 'vitest';
import {MutationObserver} from '@tanstack/react-query';
import {BusinessError, NotFoundError, SystemError} from './errors';
import {createQueryClient} from './query-client';
import {subscribeSystemErrors, type SystemErrorReport} from './system-errors';

const KEY = ['thing'];

function collectReports(): SystemErrorReport[] {
    const reports: SystemErrorReport[] = [];
    unsubscribers.push(subscribeSystemErrors((report) => {
        reports.push(report);
    }));
    return reports;
}

const unsubscribers: (() => void)[] = [];

describe(createQueryClient, () => {
    afterEach(() => {
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('hands a system error of a query to the error dialog, whose retry loads again', async () => {
        const reports = collectReports();
        const client = createQueryClient();
        const load = vi.fn<() => Promise<string>>()
            .mockRejectedValueOnce(new SystemError('server', 'boom'))
            .mockResolvedValueOnce('data');

        await client.query({queryKey: KEY, queryFn: load}).catch(() => undefined);

        expect(reports).toHaveLength(1);
        expect(reports[0]?.error.kind).toBe('server');

        reports[0]?.retry?.();

        await vi.waitFor(() => {
            expect(client.getQueryData(KEY)).toBe('data');
        });
    });

    it('reports an unexpected failure of a query as a response error', async () => {
        const reports = collectReports();
        const client = createQueryClient();

        await client.query({queryKey: KEY, queryFn: () => Promise.reject(new Error('boom'))}).catch(() => undefined);

        expect(reports[0]?.error.kind).toBe('response');
    });

    it('hands a business error of a query to the error dialog: reading has no business errors to show inline', async () => {
        const reports = collectReports();
        const client = createQueryClient();

        await client.query({queryKey: KEY, queryFn: () => Promise.reject(new BusinessError(409, 'thing.changed', []))}).catch(() => undefined);

        expect(reports).toHaveLength(1);
        expect(reports[0]?.error.kind).toBe('server');
    });

    it('does not repeat a failed query by itself', async () => {
        collectReports();
        const client = createQueryClient();
        const load = vi.fn<() => Promise<string>>(() => Promise.reject(new SystemError('network', 'down')));

        await client.query({queryKey: KEY, queryFn: load}).catch(() => undefined);

        expect(load).toHaveBeenCalledExactlyOnceWith(expect.anything());
    });

    it('keeps preloaded data fresh for the block that shows it', async () => {
        const client = createQueryClient();
        const load = vi.fn<() => Promise<string>>(() => Promise.resolve('data'));

        await client.query({queryKey: KEY, queryFn: load}).catch(() => undefined);
        await client.query({queryKey: KEY, queryFn: load});

        expect(load).toHaveBeenCalledExactlyOnceWith(expect.anything());
    });

    it('hands a system error of a mutation to the error dialog, whose retry repeats it with the same input', async () => {
        const reports = collectReports();
        const client = createQueryClient();
        const save = vi.fn<(input: string) => Promise<void>>()
            .mockRejectedValueOnce(new SystemError('server', 'boom'))
            .mockResolvedValueOnce(undefined);

        await new MutationObserver(client, {mutationFn: save}).mutate('Elite').catch(() => undefined);

        expect(reports).toHaveLength(1);

        reports[0]?.retry?.();

        await vi.waitFor(() => {
            expect(save).toHaveBeenCalledTimes(2);
        });

        expect(save.mock.calls[1]?.[0]).toBe('Elite');
    });

    it('leaves a business error of a mutation to the form that ran it', async () => {
        const reports = collectReports();
        const client = createQueryClient();

        await new MutationObserver(client, {mutationFn: () => Promise.reject(new BusinessError(409, 'thing.changed', []))})
            .mutate()
            .catch(() => undefined);

        expect(reports).toHaveLength(0);
    });

    it('leaves a missing entity of a query to the error boundary of the route', async () => {
        const reports = collectReports();
        const client = createQueryClient();

        await client.query({queryKey: KEY, queryFn: () => Promise.reject(new NotFoundError('thing.not-found'))}).catch(() => undefined);

        expect(reports).toHaveLength(0);
    });

    it('leaves a missing entity of a mutation to the error boundary of the route', async () => {
        const reports = collectReports();
        const client = createQueryClient();

        await new MutationObserver(client, {mutationFn: () => Promise.reject(new NotFoundError('thing.not-found'))})
            .mutate()
            .catch(() => undefined);

        expect(reports).toHaveLength(0);
    });
});
