import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {loadCategoryNewPage, preloadCategoryNewPage} from '../index';

const ACTION = '00000000-0000-0000-0006-000000000002';

describe('CategoryNewPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('adds a category and returns to the categories', async () => {
        stubFetch(json([{id: '00000000-0000-0000-0006-000000000001', title: 'Games', children: []}]), json({id: ACTION}, 201));
        const queryClient = createQueryClient();
        const router = createMemoryRouter([
            {path: '/categories', element: <p>Categories list</p>},
            {path: '/categories/new', loader: preloadCategoryNewPage(queryClient), lazy: {Component: loadCategoryNewPage}, HydrateFallback: () => null},
        ], {initialEntries: ['/categories/new']});
        renderWithQueries(<RouterProvider router={router}/>, queryClient);

        await expect(screen.findByRole('heading', {level: 1, name: 'Add category'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('option', {name: 'Games'})).toBeInTheDocument();

        await userEvent.type(screen.getByRole('textbox', {name: 'Title'}), 'Series');
        await userEvent.click(screen.getByRole('button', {name: 'Add category'}));

        await expect(screen.findByText('Categories list')).resolves.toBeInTheDocument();
    });
});
