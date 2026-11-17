import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {PlatformSelectionProvider} from '@/entities/platforms';
import {CurrentUserProvider} from '@/entities/users';
import {createQueryClient} from '@/shared/api';
import {json, noContent, pageResponse, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {loadAuthorsPage, preloadAuthorsPage} from '../index';

const ZED = '00000000-0000-0000-0003-000000000001';
const FINLAND = '00000000-0000-0000-0002-000000000001';

const MEMBER = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed', email: 'zed@example.com', groups: ['member'], abilities: ['add-entries']};

function renderRoute(path: string) {
    const queryClient = createQueryClient();
    const router = createMemoryRouter([
        {path: '/authors', loader: preloadAuthorsPage(queryClient), lazy: {Component: loadAuthorsPage}, HydrateFallback: () => null},
    ], {initialEntries: [path]});
    renderWithQueries(<CurrentUserProvider><PlatformSelectionProvider><RouterProvider router={router}/></PlatformSelectionProvider></CurrentUserProvider>, queryClient);
}

describe('AuthorsPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('preloads the catalog of the address, the countries and the cities of the chosen country', async () => {
        const fetchMock = stubFetchRoutes(new Map([
            ['/api/auth/me', json(MEMBER)],
            [`/api/authors?page=0&size=100&letter=Z&country=${FINLAND}`, json(pageResponse([{personId: ZED, labelId: null, name: 'Zed', note: null}], {size: 100}))],
            ['/api/countries', json([{id: FINLAND, name: 'Finland'}])],
            [`/api/countries/${FINLAND}/cities`, json([])],
        ]));

        renderRoute(`/authors?letter=Z&country=${FINLAND}`);

        await expect(screen.findByRole('heading', {level: 1, name: 'Authors'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Zed'})).toBeInTheDocument();
        expect(screen.queryByTestId('author-catalog-skeleton')).not.toBeInTheDocument();
        expect(fetchMock.mock.calls.filter(([url]) => url !== '/api/auth/me')).toHaveLength(3);
    });

    it('leads to adding an author', async () => {
        stubFetchRoutes(new Map([
            ['/api/auth/me', json(MEMBER)],
            ['/api/authors?page=0&size=100', json(pageResponse([], {size: 100}))],
            ['/api/countries', json([])],
        ]));

        renderRoute('/authors');

        await expect(screen.findByRole('link', {name: 'Add author'})).resolves.toHaveAttribute('href', '/authors/new');
    });

    it('leads to adding a label', async () => {
        stubFetchRoutes(new Map([
            ['/api/auth/me', json(MEMBER)],
            ['/api/authors?page=0&size=100', json(pageResponse([], {size: 100}))],
            ['/api/countries', json([])],
        ]));

        renderRoute('/authors');

        await expect(screen.findByRole('link', {name: 'Add label'})).resolves.toHaveAttribute('href', '/labels/new');
    });

    it('offers a public visitor no adding', async () => {
        stubFetchRoutes(new Map([
            ['/api/auth/me', noContent()],
            ['/api/authors?page=0&size=100', json(pageResponse([], {size: 100}))],
            ['/api/countries', json([])],
        ]));

        renderRoute('/authors');

        await screen.findByRole('heading', {level: 1});

        await vi.waitFor(() => {
            expect(screen.queryByRole('link', {name: 'Add author'})).not.toBeInTheDocument();
        });

        expect(screen.queryByRole('link', {name: 'Add label'})).not.toBeInTheDocument();
    });
});
