import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {loadCategoriesPage, preloadCategoriesPage} from '../index';

const GAMES = '00000000-0000-0000-0006-000000000001';

function renderRoute() {
    const queryClient = createQueryClient();
    const router = createMemoryRouter([
        {path: '/categories', loader: preloadCategoriesPage(queryClient), lazy: {Component: loadCategoriesPage}, HydrateFallback: () => null},
    ], {initialEntries: ['/categories']});
    renderWithQueries(<RouterProvider router={router}/>, queryClient);
}

describe('CategoriesPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('appears with the tree when the preload makes it in time', async () => {
        const fetchMock = stubFetch(json([{id: GAMES, title: 'Games', children: []}]));

        renderRoute();

        await expect(screen.findByRole('heading', {level: 1, name: 'Categories'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Games'})).toBeInTheDocument();
        expect(screen.queryByTestId('category-tree-skeleton')).not.toBeInTheDocument();
        expect(fetchMock).toHaveBeenCalledExactlyOnceWith('/api/categories', expect.anything());
    });

    it('leads to adding a category', async () => {
        stubFetch(json([]));

        renderRoute();

        await expect(screen.findByRole('link', {name: 'Add category'})).resolves.toHaveAttribute('href', '/categories/new');
    });
});
