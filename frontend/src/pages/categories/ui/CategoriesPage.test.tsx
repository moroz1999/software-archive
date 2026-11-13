import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {CurrentUserProvider} from '@/entities/users';
import {json, noContent, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {loadCategoriesPage, preloadCategoriesPage} from '../index';

const GAMES = '00000000-0000-0000-0006-000000000001';
const MEMBER = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed', email: 'zed@example.com', groups: ['member'], abilities: ['add-entries']};
const ADMIN = {...MEMBER, groups: ['admin', 'member'], abilities: ['add-entries', 'keep-categories']};

function stubFetch(categories: () => Response, me: () => Response = json(ADMIN)) {
    return stubFetchRoutes(new Map([['/api/categories', categories], ['/api/auth/me', me]]));
}

function renderRoute() {
    const queryClient = createQueryClient();
    const router = createMemoryRouter([
        {path: '/categories', loader: preloadCategoriesPage(queryClient), lazy: {Component: loadCategoriesPage}, HydrateFallback: () => null},
    ], {initialEntries: ['/categories']});
    renderWithQueries(<CurrentUserProvider><RouterProvider router={router}/></CurrentUserProvider>, queryClient);
}

describe('CategoriesPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('appears with the tree when the preload makes it in time', async () => {
        const fetchMock = stubFetch(json([{id: GAMES, title: 'Games', children: []}]));

        renderRoute();

        await expect(screen.findByRole('heading', {level: 1, name: 'Categories'})).resolves.toBeInTheDocument();
        await expect(screen.findByRole('link', {name: 'Games'})).resolves.toBeInTheDocument();
        expect(screen.queryByTestId('category-tree-skeleton')).not.toBeInTheDocument();
        expect(fetchMock.mock.calls.filter(([url]) => url === '/api/categories')).toHaveLength(1);
    });

    it('leads to adding a category', async () => {
        stubFetch(json([]));

        renderRoute();

        await expect(screen.findByRole('link', {name: 'Add category'})).resolves.toHaveAttribute('href', '/categories/new');
    });

    it('shows the categories to whoever may not keep them, without adding or editing', async () => {
        stubFetch(json([{id: GAMES, title: 'Games', children: []}]), json(MEMBER));

        renderRoute();

        await expect(screen.findByText('Games')).resolves.toBeInTheDocument();

        await vi.waitFor(() => {
            expect(screen.queryByRole('link', {name: 'Add category'})).not.toBeInTheDocument();
        });

        expect(screen.queryByRole('link', {name: 'Games'})).not.toBeInTheDocument();
    });

    it('shows a public visitor the categories, without adding or editing', async () => {
        stubFetch(json([{id: GAMES, title: 'Games', children: []}]), noContent());

        renderRoute();

        await expect(screen.findByText('Games')).resolves.toBeInTheDocument();

        await vi.waitFor(() => {
            expect(screen.queryByRole('link', {name: 'Add category'})).not.toBeInTheDocument();
        });

        expect(screen.queryByRole('link', {name: 'Games'})).not.toBeInTheDocument();
    });
});
