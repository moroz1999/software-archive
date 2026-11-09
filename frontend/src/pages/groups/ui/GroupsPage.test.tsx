import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {PlatformSelectionProvider} from '@/entities/platforms';
import {createQueryClient} from '@/shared/api';
import {json, pageResponse, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {loadGroupsPage, preloadGroupsPage} from '../index';

const CREW = '00000000-0000-0000-0005-000000000001';
const FINLAND = '00000000-0000-0000-0002-000000000001';

function renderRoute(path: string) {
    const queryClient = createQueryClient();
    const router = createMemoryRouter([
        {path: '/groups', loader: preloadGroupsPage(queryClient), lazy: {Component: loadGroupsPage}, HydrateFallback: () => null},
    ], {initialEntries: [path]});
    renderWithQueries(<PlatformSelectionProvider><RouterProvider router={router}/></PlatformSelectionProvider>, queryClient);
}

describe('GroupsPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('preloads the catalog of the address, the countries and the cities of the chosen country', async () => {
        const fetchMock = stubFetchRoutes(new Map([
            [`/api/groups?page=0&size=100&letter=Z&country=${FINLAND}`, json(pageResponse([{groupId: CREW, name: 'Crew', groupName: null}], {size: 100}))],
            ['/api/countries', json([{id: FINLAND, name: 'Finland'}])],
            [`/api/countries/${FINLAND}/cities`, json([])],
        ]));

        renderRoute(`/groups?letter=Z&country=${FINLAND}`);

        await expect(screen.findByRole('heading', {level: 1, name: 'Groups'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Crew'})).toBeInTheDocument();
        expect(screen.queryByTestId('group-catalog-skeleton')).not.toBeInTheDocument();
        expect(fetchMock).toHaveBeenCalledTimes(3);
    });
});
