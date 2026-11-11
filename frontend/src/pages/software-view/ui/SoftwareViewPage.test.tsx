import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {loadSoftwareViewPage, preloadSoftwareViewPage} from '../index';

const ID = '00000000-0000-0000-0001-000000000001';
const ELITE = {id: ID, title: 'Elite', description: null, releaseDate: '1984-09-20', platform: {code: 'vector', name: 'Vector'}, hardware: [], categories: []};

function renderRoute(id: string) {
    const queryClient = createQueryClient();
    const router = createMemoryRouter([
        {path: '/software/:id', loader: preloadSoftwareViewPage(queryClient), lazy: {Component: loadSoftwareViewPage}, HydrateFallback: () => null},
        {path: '/software/:id/edit', element: <p>Software editing</p>},
    ], {initialEntries: [`/software/${id}`]});
    renderWithQueries(<RouterProvider router={router}/>, queryClient);
}

describe('SoftwareViewPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('appears with the software when the preload makes it in time', async () => {
        stubFetch(json(ELITE));

        renderRoute(ID);

        await screen.findByRole('heading', {level: 1, name: 'Elite'});

        expect(screen.getByText('20 September 1984')).toBeInTheDocument();
        expect(screen.queryByTestId('software-view-skeleton')).not.toBeInTheDocument();
    });

    it('opens the editing of the software', async () => {
        stubFetch(json(ELITE));
        renderRoute(ID);

        await userEvent.click(await screen.findByRole('link', {name: 'Edit'}));

        await expect(screen.findByText('Software editing')).resolves.toBeInTheDocument();
    });
});
