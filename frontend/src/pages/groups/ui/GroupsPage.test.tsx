import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {PlatformSelectionProvider} from '@/entities/platforms';
import {CurrentUserProvider} from '@/entities/users';
import {createQueryClient} from '@/shared/api';
import {json, noContent, pageResponse, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {loadGroupsPage, preloadGroupsPage} from '../index';

const CREW = '00000000-0000-0000-0005-000000000001';
const FINLAND = '00000000-0000-0000-0002-000000000001';

const MEMBER = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed', email: 'zed@example.com', groups: ['member'], abilities: ['add-entries']};

function renderRoute(path: string) {
    const queryClient = createQueryClient();
    const router = createMemoryRouter([
        {path: '/groups', loader: preloadGroupsPage(queryClient), lazy: {Component: loadGroupsPage}, HydrateFallback: () => null},
    ], {initialEntries: [path]});
    renderWithQueries(<CurrentUserProvider><PlatformSelectionProvider><RouterProvider router={router}/></PlatformSelectionProvider></CurrentUserProvider>, queryClient);
}

describe('GroupsPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('preloads the catalog of the address, the countries and the cities of the chosen country', async () => {
        const fetchMock = stubFetchRoutes(new Map([
            ['/api/auth/me', json(MEMBER)],
            [`/api/groups?page=0&size=100&letter=Z&country=${FINLAND}`, json(pageResponse([{groupId: CREW, name: 'Crew', groupName: null}], {size: 100}))],
            ['/api/countries', json([{id: FINLAND, name: 'Finland'}])],
            [`/api/countries/${FINLAND}/cities`, json([])],
        ]));

        renderRoute(`/groups?letter=Z&country=${FINLAND}`);

        await expect(screen.findByRole('heading', {level: 1, name: 'Groups'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Crew'})).toBeInTheDocument();
        expect(screen.queryByTestId('group-catalog-skeleton')).not.toBeInTheDocument();
        expect(fetchMock.mock.calls.filter(([url]) => url !== '/api/auth/me')).toHaveLength(3);
    });

    it('leads to adding a group', async () => {
        stubFetchRoutes(new Map([
            ['/api/auth/me', json(MEMBER)],
            ['/api/groups?page=0&size=100', json(pageResponse([], {size: 100}))],
            ['/api/countries', json([])],
        ]));

        renderRoute('/groups');

        await expect(screen.findByRole('link', {name: 'Add group'})).resolves.toHaveAttribute('href', '/groups/new');
    });

    it('offers a public visitor no adding', async () => {
        stubFetchRoutes(new Map([
            ['/api/auth/me', noContent()],
            ['/api/groups?page=0&size=100', json(pageResponse([], {size: 100}))],
            ['/api/countries', json([])],
        ]));

        renderRoute('/groups');

        await screen.findByRole('heading', {level: 1});

        await vi.waitFor(() => {
            expect(screen.queryByRole('link', {name: 'Add group'})).not.toBeInTheDocument();
        });
    });
});
