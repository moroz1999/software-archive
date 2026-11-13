import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import type {UserEntry} from '@/entities/users';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, pageResponse, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {UserListProvider} from '../model/UserListProvider';
import {UserList} from './UserList';

const ZED = '00000000-0000-0000-0009-000000000001';
const AXE = '00000000-0000-0000-0009-000000000002';
const BOB = '00000000-0000-0000-0009-000000000003';

const USERS = [
    {id: AXE, name: 'Axe', email: 'axe@example.com', confirmed: false, registeredAt: '2026-02-01T10:00:00Z', groups: []},
    {id: BOB, name: 'Bob', email: 'bob@example.com', confirmed: true, registeredAt: '2026-01-15T10:00:00Z', groups: ['member', 'moderator']},
    {id: ZED, name: 'Zed', email: 'zed@example.com', confirmed: true, registeredAt: '2026-01-01T10:00:00Z', groups: ['admin', 'member']},
];

const ALL = '/api/users?page=0&size=50';

function usersPage(items: readonly unknown[], totalPages = 1, number = 0) {
    return json(pageResponse(items, {size: 50, number, totalPages, totalElements: items.length}));
}

function openList(path = '/management/users', actions?: (user: UserEntry) => string) {
    const router = createMemoryRouter([
        {path: '/management/users', element: <UserListProvider><UserList {...(actions ? {actions} : {})}/></UserListProvider>},
    ], {initialEntries: [path]});
    renderWithQueries(<RouterProvider router={router}/>);
    return router;
}

/** Every row of the table but its head, as its cells read. */
async function rows(): Promise<string[][]> {
    const table = await screen.findByRole('table', {name: 'Users'});
    return within(table).getAllByRole('row').slice(1)
        .map((row) => within(row).getAllByRole('cell').map((cell) => cell.textContent.replaceAll(/\s+/gu, ' ').trim()));
}

describe(UserList, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('shows a skeleton until the users arrive', async () => {
        stubFetchRoutes(new Map([[ALL, usersPage(USERS)]]));
        openList();

        expect(screen.getByTestId('user-list-skeleton')).toBeInTheDocument();

        await screen.findByRole('table', {name: 'Users'});

        expect(screen.queryByTestId('user-list-skeleton')).not.toBeInTheDocument();
    });

    it('shows every user with their email, the day they registered and their groups, an unconfirmed one marked', async () => {
        stubFetchRoutes(new Map([[ALL, usersPage(USERS)]]));
        openList();

        await expect(rows()).resolves.toStrictEqual([
            ['Axe Awaiting confirmation', 'axe@example.com', '1 February 2026', ''],
            ['Bob', 'bob@example.com', '15 January 2026', 'Member Moderator'],
            ['Zed', 'zed@example.com', '1 January 2026', 'Administrator Member'],
        ]);

        const table = screen.getByRole('table', {name: 'Users'});

        expect(within(table).getAllByRole('columnheader').map((cell) => cell.textContent))
            .toStrictEqual(['User name', 'Email', 'Registered', 'Groups']);
    });

    it('puts the actions given into the row of every user', async () => {
        stubFetchRoutes(new Map([[ALL, usersPage(USERS)]]));
        openList('/management/users', (user) => `Act on ${user.name}`);

        await expect(rows()).resolves.toContainEqual(['Bob', 'bob@example.com', '15 January 2026', 'Member Moderator', 'Act on Bob']);
    });

    it('shows the users of the search in the address, the search in its field', async () => {
        const fetchMock = stubFetchRoutes(new Map([[`${ALL}&query=ze`, usersPage([USERS[2]])]]));
        openList('/management/users?query=ze');

        await expect(rows()).resolves.toHaveLength(1);

        expect(screen.getByRole('searchbox', {name: 'Find a user'})).toHaveValue('ze');
        expect(fetchMock.mock.calls.map(([url]) => url)).toStrictEqual([`${ALL}&query=ze`]);
    });

    it('goes to the address of a search sent, from its first page', async () => {
        stubFetchRoutes(new Map([[ALL, usersPage(USERS, 3)], [`${ALL}&query=bob`, usersPage([USERS[1]])], [`/api/users?page=1&size=50`, usersPage(USERS, 3, 1)]]));
        const router = openList('/management/users?page=2');
        await rows();

        await userEvent.type(screen.getByRole('searchbox', {name: 'Find a user'}), 'bob');
        await userEvent.click(screen.getByRole('button', {name: 'Find'}));

        await vi.waitFor(async () => {
            await expect(rows()).resolves.toHaveLength(1);
        });

        expect(`${router.state.location.pathname}${router.state.location.search}`).toBe('/management/users?query=bob');
    });

    it('leads to the other pages of the search', async () => {
        stubFetchRoutes(new Map([[`${ALL}&query=e`, usersPage(USERS, 3)]]));
        openList('/management/users?query=e');

        const pages = await screen.findByRole('navigation', {name: 'Pages'});

        expect(within(pages).getByRole('link', {name: '2'})).toHaveAttribute('href', '/management/users?query=e&page=2');
        expect(within(pages).getByText('1')).toHaveAttribute('aria-current', 'page');
    });

    it('says so when no user matches', async () => {
        stubFetchRoutes(new Map([[`${ALL}&query=qq`, usersPage([])]]));
        openList('/management/users?query=qq');

        await expect(screen.findByText('No users found.')).resolves.toBeInTheDocument();
        expect(screen.queryByRole('table', {name: 'Users'})).not.toBeInTheDocument();
    });

    it('hands a refusal of the list to the error dialog and shows nothing of its own', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetchRoutes(new Map([[ALL, json({status: 403, code: 'access.denied'}, 403)]]));
        openList();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(screen.queryByRole('table', {name: 'Users'})).not.toBeInTheDocument();
        expect(screen.queryByTestId('user-list-skeleton')).not.toBeInTheDocument();
    });

    it('hands a system error of the list to the error dialog and shows nothing of its own', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetchRoutes(new Map([[ALL, json({status: 500}, 500)]]));
        openList();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(reports[0]?.error.kind).toBe('server');
        expect(screen.queryByTestId('user-list-skeleton')).not.toBeInTheDocument();
    });
});
