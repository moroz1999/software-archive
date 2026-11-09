import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {PlatformSelectionProvider, PlatformSwitch} from '@/entities/platforms';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, pageResponse, stubFetch, stubFetchRoutes, renderWithQueries} from '@/shared/lib/test';
import {LatestSoftwareProvider} from '../model/LatestSoftwareProvider';
import {LatestSoftware} from './LatestSoftware';

const ELITE = {id: '00000000-0000-0000-0001-000000000001', title: 'Elite', releaseDate: '1984-09-20'};
const MINER = {id: '00000000-0000-0000-0001-000000000002', title: 'Manic Miner', releaseDate: null};

function renderLatest({withSwitch = false} = {}) {
    renderWithQueries(
        <MemoryRouter><PlatformSelectionProvider>{withSwitch && <PlatformSwitch/>}<LatestSoftwareProvider><LatestSoftware/></LatestSoftwareProvider></PlatformSelectionProvider></MemoryRouter>,
    );
}

describe(LatestSoftware, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        localStorage.clear();
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('shows a skeleton until the latest software arrives', async () => {
        const fetchMock = stubFetch(json(pageResponse([ELITE], {size: 12})));
        renderLatest();

        expect(screen.getByTestId('latest-software-skeleton')).toBeInTheDocument();
        await expect(screen.findByRole('article', {name: 'Elite'})).resolves.toHaveTextContent('1984');
        expect(screen.queryByTestId('latest-software-skeleton')).not.toBeInTheDocument();
        expect(fetchMock).toHaveBeenCalledExactlyOnceWith('/api/software/latest?size=12', expect.anything());
    });

    it('shows the latest software of the chosen platform and follows the switch', async () => {
        localStorage.setItem('archive.platform', 'bk');
        const fetchMock = stubFetchRoutes(new Map([
            ['/api/platforms', json([{code: 'agat', name: 'Agat'}, {code: 'bk', name: 'BK'}])],
            ['/api/software/latest?size=12&platform=bk', json(pageResponse([ELITE], {size: 12}))],
            ['/api/software/latest?size=12&platform=agat', json(pageResponse([MINER], {size: 12}))],
        ]));
        renderLatest({withSwitch: true});

        await screen.findByRole('article', {name: 'Elite'});
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Platform'}), await screen.findByRole('option', {name: 'Agat'}));

        await expect(screen.findByRole('article', {name: 'Manic Miner'})).resolves.toBeInTheDocument();
        expect(fetchMock.mock.calls.map(([url]) => url)).toContain('/api/software/latest?size=12&platform=agat');
    });

    it('keeps the shown software under a spinner, without a skeleton, while another platform loads', async () => {
        let answerAgat: (() => void) | undefined;
        const fetchMock = stubFetchRoutes(new Map([
            ['/api/platforms', json([{code: 'agat', name: 'Agat'}])],
            ['/api/software/latest?size=12', json(pageResponse([ELITE], {size: 12}))],
        ]));
        const original = fetchMock.getMockImplementation();
        fetchMock.mockImplementation(async (input, init) => {
            if (input === '/api/software/latest?size=12&platform=agat') {
                await new Promise<void>((resolve) => {
                    answerAgat = resolve;
                });
                return json(pageResponse([MINER], {size: 12}))();
            }
            return original === undefined ? Promise.reject(new Error('no stub')) : original(input, init);
        });
        renderLatest({withSwitch: true});
        await screen.findByRole('article', {name: 'Elite'});

        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Platform'}), await screen.findByRole('option', {name: 'Agat'}));

        await vi.waitFor(() => {
            expect(screen.getByTestId('loading-state-spinner')).toHaveAttribute('aria-hidden', 'false');
        });

        expect(screen.queryByTestId('latest-software-skeleton')).not.toBeInTheDocument();
        expect(screen.getByRole('article', {name: 'Elite'})).toBeInTheDocument();

        answerAgat?.();

        await expect(screen.findByRole('article', {name: 'Manic Miner'})).resolves.toBeInTheDocument();
        expect(screen.getByTestId('loading-state-spinner')).toHaveAttribute('aria-hidden', 'true');
    });

    it('lists the software as cards leading to their pages, in the order given', async () => {
        stubFetch(json(pageResponse([MINER, ELITE], {size: 12})));
        renderLatest();

        await expect(screen.findByRole('link', {name: 'Manic Miner'})).resolves.toHaveAttribute('href', `/software/${MINER.id}`);
        expect(screen.getAllByRole('heading', {level: 3}).map((title) => title.textContent)).toStrictEqual(['Manic Miner', 'Elite']);
    });

    it('says so when the archive has no software', async () => {
        stubFetch(json(pageResponse([], {size: 12})));
        renderLatest();

        await expect(screen.findByText('No software in the archive yet.')).resolves.toBeInTheDocument();
    });

    it('hands a system error to the error dialog and shows nothing of its own', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetch(json({status: 500}, 500));
        renderLatest();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(reports[0]?.error.kind).toBe('server');
        expect(screen.queryByTestId('latest-software-skeleton')).not.toBeInTheDocument();
    });
});
