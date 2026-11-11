import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {loadAuthorEditPage, preloadAuthorEditPage} from '../index';

const ID = '00000000-0000-0000-0003-000000000001';
const ZED = {id: ID, name: 'Zed', realName: 'John Doe', notes: null, labels: [], locations: [], groups: []};

/** What the page loads besides the author: the countries of the locations editor. */
const AROUND = new Map([['/api/countries', json([])]]);

function renderRoute() {
    const queryClient = createQueryClient();
    const router = createMemoryRouter([
        {path: '/authors', element: <p>Authors list</p>},
        {path: '/authors/:id', element: <p>Author view</p>},
        {path: '/authors/:id/edit', loader: preloadAuthorEditPage(queryClient), lazy: {Component: loadAuthorEditPage}, HydrateFallback: () => null},
    ], {initialEntries: [`/authors/${ID}/edit`]});
    renderWithQueries(<RouterProvider router={router}/>, queryClient);
}

describe('AuthorEditPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('appears with the author in its form when the preload makes it in time', async () => {
        stubFetchAround(AROUND, json(ZED));

        renderRoute();

        await screen.findByRole('heading', {level: 1, name: 'Edit author'});

        expect(screen.getByRole('textbox', {name: 'Real name'})).toHaveValue('John Doe');
        expect(screen.queryByTestId('author-editor-skeleton')).not.toBeInTheDocument();
    });

    it('returns to the author after saving', async () => {
        stubFetchAround(AROUND, json(ZED), noContent());
        renderRoute();

        await userEvent.click(await screen.findByRole('button', {name: 'Save'}));

        await expect(screen.findByText('Author view')).resolves.toBeInTheDocument();
    });

    it('returns to the catalog after deleting', async () => {
        stubFetchAround(AROUND, json(ZED), noContent());
        renderRoute();

        await userEvent.click(await screen.findByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await expect(screen.findByText('Authors list')).resolves.toBeInTheDocument();
    });
});
