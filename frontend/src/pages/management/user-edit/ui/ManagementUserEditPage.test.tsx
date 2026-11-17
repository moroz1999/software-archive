import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {loadManagementUserEditPage, preloadManagementUserEditPage} from '../index';

const BOB = '00000000-0000-0000-0009-000000000003';
const BOB_ENTRY = {id: BOB, name: 'Bob', email: 'bob@example.com', confirmed: true, registeredAt: '2026-01-15T10:00:00Z', groups: ['member']};

function renderRoute(path = `/management/users/${BOB}/edit`) {
    const queryClient = createQueryClient();
    const router = createMemoryRouter([
        {path: '/management/users', element: <p>Users list</p>},
        {
            path: '/management/users/:id/edit',
            loader: preloadManagementUserEditPage(queryClient),
            lazy: {Component: loadManagementUserEditPage},
            HydrateFallback: () => null,
            errorElement: <p>Not found page</p>,
        },
    ], {initialEntries: [path]});
    renderWithQueries(<RouterProvider router={router}/>, queryClient);
}

describe('ManagementUserEditPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('appears with the user in its form when the preload makes it in time', async () => {
        const fetchMock = stubFetchRoutes(new Map([[`/api/users/${BOB}`, json(BOB_ENTRY)]]));

        renderRoute();

        await screen.findByRole('heading', {level: 1, name: 'Edit user'});
        const management = within(screen.getByRole('navigation', {name: 'Management'}));

        expect(management.getByRole('link', {name: 'Users'})).toHaveAttribute('aria-current', 'page');
        expect(screen.getByRole('textbox', {name: 'User name'})).toHaveValue('Bob');
        expect(screen.getByRole('link', {name: 'Cancel'})).toHaveAttribute('href', '/management/users');
        expect(screen.queryByTestId('user-edit-skeleton')).not.toBeInTheDocument();
        expect(fetchMock.mock.calls.filter(([url]) => url === `/api/users/${BOB}`)).toHaveLength(1);
    });

    it('shows a skeleton while the user is slow to come', async () => {
        const fetchMock = stubFetchRoutes(new Map());
        fetchMock.mockImplementation(() => new Promise(() => undefined));

        renderRoute();

        await expect(screen.findByTestId('user-edit-skeleton', {}, {timeout: 2000})).resolves.toBeInTheDocument();
        expect(screen.getByRole('heading', {level: 1, name: 'Edit user'})).toBeInTheDocument();
    });

    it('returns to the users after saving', async () => {
        stubFetchRoutes(new Map([[`/api/users/${BOB}`, (() => {
            let calls = 0;
            return () => {
                calls += 1;
                return calls === 1 ? json(BOB_ENTRY)() : noContent()();
            };
        })()]]));
        renderRoute();

        await userEvent.click(await screen.findByRole('button', {name: 'Save'}));

        await expect(screen.findByText('Users list')).resolves.toBeInTheDocument();
    });

    it('is a page that does not exist for an unknown user', async () => {
        stubFetchRoutes(new Map([[`/api/users/${BOB}`, json({status: 404, code: 'user.not-found'}, 404)]]));

        renderRoute();

        await expect(screen.findByText('Not found page')).resolves.toBeInTheDocument();
    });

    it('is a page that does not exist for an id that cannot be one', async () => {
        const fetchMock = stubFetchRoutes(new Map());

        renderRoute('/management/users/bob/edit');

        await expect(screen.findByText('Not found page')).resolves.toBeInTheDocument();
        expect(fetchMock).not.toHaveBeenCalled();
    });
});
