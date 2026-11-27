import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {PlatformSelectionProvider} from '@/entities/platforms';
import {CurrentUserProvider} from '@/entities/users';
import {type components, createQueryClient} from '@/shared/api';
import {json, noContent, pageResponse, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {SOFTWARE_CATEGORY_ROUTE} from '@/features/software/catalog';
import {loadSoftwarePage, preloadSoftwarePage} from '../index';

const ALPHA = {id: '00000000-0000-0000-0001-000000000001', title: 'Fixture Software Alpha', releaseDate: '1985-03-02', screenshots: [], madeBy: [], rating: 0, votesAmount: 0} satisfies components['schemas']['SoftwareListItemDto'];
const GAMES = '00000000-0000-0000-00fd-0000000000c1';
const FACETS = {years: [{value: 1985, count: 1}], countries: [], cities: [], platforms: [], hardware: []};

function catalog() {
    return json({...pageResponse([ALPHA]), facets: FACETS});
}

const MEMBER = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed', email: 'zed@example.com', groups: ['member'], abilities: ['add-entries']};

const ROUTES = new Map([
    ['/api/categories', json([{id: GAMES, title: 'Games', children: []}])],
    ['/api/countries', json([])],
    ['/api/platforms', json([])],
    ['/api/auth/me', json(MEMBER)],
]);

function renderRoute(path = '/software') {
    const queryClient = createQueryClient();
    const router = createMemoryRouter([
        {path: '/software', loader: preloadSoftwarePage(queryClient), lazy: {Component: loadSoftwarePage}, HydrateFallback: () => null},
        {path: SOFTWARE_CATEGORY_ROUTE, loader: preloadSoftwarePage(queryClient), lazy: {Component: loadSoftwarePage}, HydrateFallback: () => null},
    ], {initialEntries: [path]});
    renderWithQueries(<CurrentUserProvider><PlatformSelectionProvider><RouterProvider router={router}/></PlatformSelectionProvider></CurrentUserProvider>, queryClient);
}

describe('SoftwarePage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows the catalog under the page title', async () => {
        stubFetchRoutes(new Map([...ROUTES, ['/api/software?page=0', catalog()]]));

        renderRoute();

        await expect(screen.findByRole('heading', {level: 1, name: 'Software'})).resolves.toBeInTheDocument();
        await expect(screen.findByRole('article', {name: 'Fixture Software Alpha'})).resolves.toBeInTheDocument();
        await expect(screen.findByRole('link', {name: 'Games'})).resolves.toBeInTheDocument();
    });

    it('offers to add software and leads from a card to its software', async () => {
        stubFetchRoutes(new Map([...ROUTES, ['/api/software?page=0', catalog()]]));

        renderRoute();

        await expect(screen.findByRole('link', {name: 'Add software'})).resolves.toHaveAttribute('href', '/software/new');
        await expect(screen.findByRole('link', {name: 'Fixture Software Alpha'})).resolves.toHaveAttribute('href', `/software/${ALPHA.id}`);
    });

    it('preloads the catalog of the address, the categories and the countries, and appears with them in time', async () => {
        const fetchMock = stubFetchRoutes(new Map([...ROUTES, ['/api/software?page=0&year=1985', catalog()]]));

        renderRoute('/software?year=1985');

        await screen.findByRole('heading', {level: 1, name: 'Software'});

        expect(screen.getByRole('article', {name: 'Fixture Software Alpha'})).toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Games'})).toBeInTheDocument();
        expect(screen.queryByTestId('software-catalog-skeleton')).not.toBeInTheDocument();
        expect(fetchMock.mock.calls.map(([url]) => url)).toStrictEqual(expect.arrayContaining([
            '/api/software?page=0&year=1985', '/api/categories', '/api/countries',
        ]));
    });

    it('preloads the catalog of the category in the path', async () => {
        const fetchMock = stubFetchRoutes(new Map([...ROUTES, [`/api/software?page=0&category=${GAMES}`, catalog()]]));

        renderRoute(`/software/category/${GAMES}`);

        await screen.findByRole('heading', {level: 1, name: 'Software'});

        expect(screen.getByRole('article', {name: 'Fixture Software Alpha'})).toBeInTheDocument();
        expect(fetchMock.mock.calls.map(([url]) => url)).toContain(`/api/software?page=0&category=${GAMES}`);
    });

    it('appears with a skeleton when the preload takes longer than the window', async () => {
        let respond: (response: Response) => void = () => undefined;
        const fetchMock = stubFetchRoutes(ROUTES);
        const base = fetchMock.getMockImplementation();
        fetchMock.mockImplementation((input, init) => {
            if (input === '/api/software?page=0') {
                return new Promise((resolve) => {
                    respond = resolve;
                });
            }
            return base === undefined ? Promise.reject(new Error('no route')) : base(input, init);
        });

        renderRoute();

        await expect(screen.findByTestId('software-catalog-skeleton', {}, {timeout: 1000})).resolves.toBeInTheDocument();

        respond(catalog()());

        await expect(screen.findByRole('article', {name: 'Fixture Software Alpha'})).resolves.toBeInTheDocument();
    });

    it('offers a public visitor no adding', async () => {
        stubFetchRoutes(new Map([...ROUTES, ['/api/auth/me', noContent()], ['/api/software?page=0', catalog()]]));

        renderRoute();

        await screen.findByRole('article', {name: 'Fixture Software Alpha'});

        expect(screen.queryByRole('link', {name: 'Add software'})).not.toBeInTheDocument();
    });
});
