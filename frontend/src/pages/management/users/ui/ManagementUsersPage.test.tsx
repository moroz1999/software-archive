import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {CurrentUserProvider} from '@/entities/users';
import {json, noContent, pageResponse, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {loadManagementUsersPage, preloadManagementUsersPage, shouldPreloadManagementUsersPage} from '../index';

const ADMIN = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed', email: 'zed@example.com', groups: ['admin', 'member'], abilities: ['add-entries', 'keep-categories']};
const BOB = '00000000-0000-0000-0009-000000000003';
const AXE = '00000000-0000-0000-0009-000000000002';
const USERS = [
    {id: AXE, name: 'Axe', email: 'axe@example.com', confirmed: false, registeredAt: '2026-02-01T10:00:00Z', groups: []},
    {id: BOB, name: 'Bob', email: 'bob@example.com', confirmed: true, registeredAt: '2026-01-15T10:00:00Z', groups: ['member']},
    {id: ADMIN.id, name: 'Zed', email: 'zed@example.com', confirmed: true, registeredAt: '2026-01-01T10:00:00Z', groups: ['admin', 'member']},
];
const ALL = '/api/users?page=0&size=50';

function usersPage(items: readonly unknown[]) {
    return json(pageResponse(items, {size: 50, totalElements: items.length}));
}

function renderRoute(path = '/management/users') {
    const queryClient = createQueryClient();
    const router = createMemoryRouter([
        {
            path: '/management/users',
            loader: preloadManagementUsersPage(queryClient),
            shouldRevalidate: shouldPreloadManagementUsersPage,
            lazy: {Component: loadManagementUsersPage},
            HydrateFallback: () => null,
        },
    ], {initialEntries: [path]});
    renderWithQueries(<CurrentUserProvider><RouterProvider router={router}/></CurrentUserProvider>, queryClient);
}

describe('ManagementUsersPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('appears with the users when the preload makes it in time', async () => {
        const fetchMock = stubFetchRoutes(new Map([[ALL, usersPage(USERS)], ['/api/auth/me', json(ADMIN)]]));

        renderRoute();

        await expect(screen.findByRole('heading', {level: 1, name: 'Users'})).resolves.toBeInTheDocument();
        await expect(screen.findByRole('table', {name: 'Users'})).resolves.toBeInTheDocument();
        expect(screen.queryByTestId('user-list-skeleton')).not.toBeInTheDocument();
        expect(fetchMock.mock.calls.filter(([url]) => url === ALL)).toHaveLength(1);
    });

    it('preloads the search of the address', async () => {
        const fetchMock = stubFetchRoutes(new Map([[`${ALL}&query=bob`, usersPage([USERS[1]])], ['/api/auth/me', json(ADMIN)]]));

        renderRoute('/management/users?query=bob');

        await screen.findByRole('table', {name: 'Users'});

        expect(fetchMock.mock.calls.filter(([url]) => url === `${ALL}&query=bob`)).toHaveLength(1);
    });

    it('shows the users among the management sections', async () => {
        stubFetchRoutes(new Map([[ALL, usersPage(USERS)], ['/api/auth/me', json(ADMIN)]]));

        renderRoute();

        const management = within(await screen.findByRole('navigation', {name: 'Management'}));

        expect(management.getByRole('link', {name: 'Users'})).toHaveAttribute('aria-current', 'page');
        expect(management.getByRole('link', {name: 'Categories'})).toHaveAttribute('href', '/management/categories');
        expect(management.getByRole('link', {name: 'Imports'})).toHaveAttribute('href', '/management/imports');
        expect(management.getAllByRole('link')).toHaveLength(3);
    });

    it('offers to change the groups of every confirmed user', async () => {
        stubFetchRoutes(new Map([[ALL, usersPage(USERS)], ['/api/auth/me', json(ADMIN)]]));

        renderRoute();
        await screen.findByRole('table', {name: 'Users'});

        expect(screen.getByRole('button', {name: 'Change groups of Bob'})).toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Change groups of Zed'})).toBeInTheDocument();
        expect(screen.queryByRole('button', {name: 'Change groups of Axe'})).not.toBeInTheDocument();
    });

    it('shows the groups given once they are saved', async () => {
        let listed = 0;
        stubFetchRoutes(new Map([
            [ALL, () => {
                listed += 1;
                return usersPage(listed === 1 ? USERS : USERS.map((user) => (user.id === BOB ? {...user, groups: ['member', 'moderator']} : user)))();
            }],
            [`/api/users/${BOB}/groups`, noContent()],
            ['/api/auth/me', json(ADMIN)],
        ]));

        renderRoute();
        await screen.findByRole('table', {name: 'Users'});

        await userEvent.click(screen.getByRole('button', {name: 'Change groups of Bob'}));
        const dialog = screen.getByRole('dialog', {name: 'Groups of Bob'});
        await userEvent.click(within(dialog).getByRole('checkbox', {name: 'Moderator'}));
        await userEvent.click(within(dialog).getByRole('button', {name: 'Save'}));

        const table = screen.getByRole('table', {name: 'Users'});
        await vi.waitFor(() => {
            expect(within(table).getByRole('row', {name: /Bob/u})).toHaveTextContent('Moderator');
        });
    });
});
