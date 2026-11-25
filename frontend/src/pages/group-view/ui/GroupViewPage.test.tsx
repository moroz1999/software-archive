import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {json, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {loadGroupViewPage, preloadGroupViewPage} from '../index';

const ID = '00000000-0000-0000-0005-000000000001';
const CREW = {id: ID, name: 'Crew', notes: null, types: [], labels: [], locations: [], members: [], memberGroups: [], groups: [], origins: [], added: {by: 'Zed', at: '2026-01-01T00:00:00Z'}, canChange: true};

describe('GroupViewPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('appears with the group when the preload makes it in time', async () => {
        const fetchMock = stubFetchAround(new Map([[`/api/groups/${ID}/software`, json([])], [`/api/groups/${ID}/releases`, json([])]]), json(CREW));
        const queryClient = createQueryClient();
        const router = createMemoryRouter([
            {path: '/groups/:id', loader: preloadGroupViewPage(queryClient), lazy: {Component: loadGroupViewPage}, HydrateFallback: () => null},
        ], {initialEntries: [`/groups/${ID}`]});
        renderWithQueries(<RouterProvider router={router}/>, queryClient);

        await expect(screen.findByRole('heading', {level: 1, name: 'Crew'})).resolves.toBeInTheDocument();
        expect(screen.queryByTestId('group-view-skeleton')).not.toBeInTheDocument();
        expect(fetchMock).toHaveBeenCalledExactlyOnceWith(`/api/groups/${ID}`, expect.anything());
    });
});
