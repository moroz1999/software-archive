import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, waitFor, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {PlatformSelectionProvider} from '@/entities/platforms';
import {createQueryClient, subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {loadSearchPage, preloadSearchPage, shouldPreloadSearchPage} from '../index';

const ELITE = {id: '00000000-0000-0000-00fd-000000000001', title: 'Elite', releaseDate: '1987-05-01'};
const ZED = {id: '00000000-0000-0000-00fd-000000000011', name: 'Zed', realName: 'John Doe'};
const SQUAD = {id: '00000000-0000-0000-00fd-000000000021', name: 'ZS'};

function found(page = 0, sets: readonly unknown[] = [
    {type: 'author', total: 52, items: [ZED]},
    {type: 'group', total: 1, items: [SQUAD]},
    {type: 'software', total: 51, items: [ELITE]},
]) {
    return json({sets, page: {size: 50, number: page, totalElements: 104, totalPages: 3}});
}

const NOTHING = json({sets: [], page: {size: 50, number: 0, totalElements: 0, totalPages: 0}});

function routes(answers: [string, () => Response][]): Map<string, () => Response> {
    return new Map([['/api/platforms', json([])], ...answers]);
}

function renderRoute(path: string) {
    const queryClient = createQueryClient();
    const router = createMemoryRouter([
        {path: '/search', loader: preloadSearchPage(queryClient), shouldRevalidate: shouldPreloadSearchPage, lazy: {Component: loadSearchPage}, HydrateFallback: () => null},
    ], {initialEntries: [path]});
    renderWithQueries(<PlatformSelectionProvider><RouterProvider router={router}/></PlatformSelectionProvider>, queryClient);
    return {router};
}

function asked(fetchMock: {mock: {calls: unknown[][]}}): unknown[] {
    return fetchMock.mock.calls.map(([url]) => url).filter((url) => typeof url === 'string' && url !== '/api/platforms');
}

describe('SearchPage', () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        localStorage.clear();
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('preloads the matches and shows them titled with the keyword', async () => {
        stubFetchRoutes(routes([['/api/search?q=z&page=0', found()]]));

        renderRoute('/search?q=z');

        await screen.findByRole('heading', {level: 1, name: 'Search results for “z”'});

        expect(screen.queryByTestId('full-search-skeleton')).not.toBeInTheDocument();
        expect(screen.queryByRole('searchbox')).not.toBeInTheDocument();
    });

    it('shows people and groups as tables and software as cards, each kind with the count of all its matches', async () => {
        stubFetchRoutes(routes([['/api/search?q=z&page=0', found()]]));

        renderRoute('/search?q=z');

        const authors = await screen.findByRole('region', {name: 'Authors (52)'});
        const groups = screen.getByRole('region', {name: 'Groups (1)'});

        expect(within(within(authors).getByRole('table')).getByRole('row', {name: 'Zed John Doe'})).toBeInTheDocument();
        expect(within(authors).getByRole('link', {name: 'Zed'})).toHaveAttribute('href', `/authors/${ZED.id}`);
        expect(within(groups).getByRole('link', {name: 'ZS'})).toHaveAttribute('href', `/groups/${SQUAD.id}`);
        expect(within(screen.getByRole('region', {name: 'Software (51)'})).getByRole('article', {name: 'Elite'})).toBeInTheDocument();
    });

    it('lists the kinds in order: people, groups, software', async () => {
        stubFetchRoutes(routes([['/api/search?q=z&page=0', found()]]));

        renderRoute('/search?q=z');

        await screen.findByRole('region', {name: 'Authors (52)'});

        expect(screen.getAllByRole('heading', {level: 2}).map((heading) => heading.textContent)).toStrictEqual([
            'Authors (52)', 'Groups (1)', 'Software (51)',
        ]);
    });

    it('pages the one list, the page in the title', async () => {
        stubFetchRoutes(routes([['/api/search?q=z&page=1', found(1)]]));

        renderRoute('/search?q=z&page=2');

        await screen.findByRole('heading', {level: 1, name: 'Search results for “z”, page 2'});
        const pages = screen.getAllByRole('navigation', {name: 'Pages'});

        expect(pages).toHaveLength(1);
        expect(within(pages[0] ?? document.body).getByRole('link', {name: '3'})).toHaveAttribute('href', '/search?q=z&page=3');
    });

    it('searches the kinds chosen, every kind checked at first', async () => {
        stubFetchRoutes(routes([['/api/search?q=z&page=0', found()]]));

        renderRoute('/search?q=z');

        await screen.findByRole('region', {name: 'Authors (52)'});

        expect(screen.getByRole('checkbox', {name: 'Authors'})).toBeChecked();
        expect(screen.getByRole('checkbox', {name: 'Groups'})).toBeChecked();
        expect(screen.getByRole('checkbox', {name: 'Software'})).toBeChecked();
    });

    it('leaves a kind out by its box, from the first page', async () => {
        const fetchMock = stubFetchRoutes(routes([
            ['/api/search?q=z&page=1', found(1)],
            ['/api/search?q=z&types=group%2Csoftware&page=0', found(0, [{type: 'group', total: 1, items: [SQUAD]}])],
        ]));
        const {router} = renderRoute('/search?q=z&page=2');
        await screen.findByRole('region', {name: 'Authors (52)'});

        await userEvent.click(screen.getByRole('checkbox', {name: 'Authors'}));

        await waitFor(() => {
            expect(router.state.location.search).toBe('?q=z&types=group%2Csoftware');
        });
        await waitFor(() => {
            expect(screen.queryByRole('region', {name: 'Authors (52)'})).not.toBeInTheDocument();
        });

        expect(asked(fetchMock)).toContain('/api/search?q=z&types=group%2Csoftware&page=0');
    });

    it('keeps the last kind chosen', async () => {
        stubFetchRoutes(routes([['/api/search?q=z&types=group&page=0', found(0, [{type: 'group', total: 1, items: [SQUAD]}])]]));

        renderRoute('/search?q=z&types=group');

        await screen.findByRole('region', {name: 'Groups (1)'});

        expect(screen.getByRole('checkbox', {name: 'Groups'})).toBeDisabled();
        expect(screen.getByRole('checkbox', {name: 'Authors'})).not.toBeChecked();
    });

    it('chooses every kind again at once', async () => {
        stubFetchRoutes(routes([
            ['/api/search?q=z&types=group&page=1', found(1, [{type: 'group', total: 1, items: [SQUAD]}])],
            ['/api/search?q=z&page=0', found()],
        ]));
        const {router} = renderRoute('/search?q=z&types=group&page=2');
        await screen.findByRole('region', {name: 'Groups (1)'});

        await userEvent.click(screen.getByRole('button', {name: 'Select all'}));

        await waitFor(() => {
            expect(router.state.location.search).toBe('?q=z');
        });
    });

    it('asks for the chosen platform', async () => {
        localStorage.setItem('archive.platform', 'bk');
        const fetchMock = stubFetchRoutes(routes([['/api/search?q=z&page=0&platform=bk', found()]]));

        renderRoute('/search?q=z');

        await screen.findByRole('region', {name: 'Authors (52)'});

        expect(asked(fetchMock)).toContain('/api/search?q=z&page=0&platform=bk');
    });

    it('says when nothing is found', async () => {
        stubFetchRoutes(routes([['/api/search?q=zzz&page=0', NOTHING]]));

        renderRoute('/search?q=zzz');

        await expect(screen.findByText('Nothing found.')).resolves.toBeInTheDocument();
        expect(screen.queryByRole('navigation', {name: 'Pages'})).not.toBeInTheDocument();
    });

    it('asks nothing without a keyword', async () => {
        const fetchMock = stubFetchRoutes(routes([]));

        renderRoute('/search');

        await screen.findByRole('heading', {level: 1, name: 'Search'});

        expect(asked(fetchMock)).toStrictEqual([]);
        expect(screen.queryByRole('region')).not.toBeInTheDocument();
    });

    it('shows a skeleton while the first matches are slow to come', async () => {
        const fetchMock = stubFetchRoutes(routes([]));
        fetchMock.mockImplementation((input) => (input === '/api/platforms' ? Promise.resolve(json([])()) : new Promise(() => undefined)));

        renderRoute('/search?q=z');

        await expect(screen.findByTestId('full-search-skeleton', {}, {timeout: 2000})).resolves.toBeInTheDocument();
    });

    it('reports a system error and shows no matches', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => reports.push(report)));
        stubFetchRoutes(routes([['/api/search?q=z&page=0', json({detail: 'Internal server error.'}, 500)]]));

        renderRoute('/search?q=z');

        await waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(screen.queryByRole('region')).not.toBeInTheDocument();
    });
});
