import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {loadCategoryEditPage, preloadCategoryEditPage} from '../index';

const GAMES = '00000000-0000-0000-0006-000000000001';
const ACTION = '00000000-0000-0000-0006-000000000002';

/** What the page loads besides the category: the tree to choose the parent from. */
const AROUND = new Map([['/api/categories', json([{id: GAMES, title: 'Games', children: [{id: ACTION, title: 'Action', children: []}]}])]]);

function renderRoute(path = `/categories/${ACTION}/edit`) {
    const queryClient = createQueryClient();
    const router = createMemoryRouter([
        {path: '/categories', element: <p>Categories list</p>},
        {
            path: '/categories/:id/edit',
            loader: preloadCategoryEditPage(queryClient),
            lazy: {Component: loadCategoryEditPage},
            HydrateFallback: () => null,
            errorElement: <p>Not found page</p>,
        },
    ], {initialEntries: [path]});
    renderWithQueries(<RouterProvider router={router}/>, queryClient);
}

describe('CategoryEditPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('appears with the category in its form when the preload makes it in time', async () => {
        stubFetchAround(AROUND, json({id: ACTION, title: 'Action', parentId: GAMES}));

        renderRoute();

        await screen.findByRole('heading', {level: 1, name: 'Edit category'});

        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveValue('Action');
        expect(screen.getByRole('option', {name: 'Games', selected: true})).toBeInTheDocument();
        expect(screen.queryByTestId('category-editor-skeleton')).not.toBeInTheDocument();
    });

    it('returns to the categories after saving', async () => {
        stubFetchAround(AROUND, json({id: ACTION, title: 'Action', parentId: GAMES}), noContent());
        renderRoute();

        await userEvent.click(await screen.findByRole('button', {name: 'Save'}));

        await expect(screen.findByText('Categories list')).resolves.toBeInTheDocument();
    });

    it('returns to the categories after deleting', async () => {
        stubFetchAround(AROUND, json({id: ACTION, title: 'Action', parentId: GAMES}), noContent());
        renderRoute();

        await userEvent.click(await screen.findByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await expect(screen.findByText('Categories list')).resolves.toBeInTheDocument();
    });

    it('is a page that does not exist for an id that cannot be one', async () => {
        const fetchMock = stubFetchAround(AROUND, noContent());

        renderRoute('/categories/games/edit');

        await expect(screen.findByText('Not found page')).resolves.toBeInTheDocument();
        expect(fetchMock).not.toHaveBeenCalled();
    });
});
