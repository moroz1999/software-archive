import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {loadSoftwareEditPage, preloadSoftwareEditPage} from '../index';

const ID = '00000000-0000-0000-0001-000000000001';
const ELITE = {id: ID, title: 'Elite', description: null, releaseDate: null, platform: {code: 'vector', name: 'Vector'}, hardware: []};

/** What the page loads besides the software: the platforms and the hardware of the software's platform. */
const AROUND = new Map([
    ['/api/platforms', json([{code: 'vector', name: 'Vector'}])],
    ['/api/hardware?platform=vector', json([])],
]);

function stubFetch(...responses: (() => Response)[]) {
    return stubFetchAround(AROUND, ...responses);
}

function renderRoute(id: string) {
    const queryClient = createQueryClient();
    const router = createMemoryRouter([
        {path: '/software', element: <p>Software list</p>},
        {path: '/software/:id', element: <p>Software view</p>},
        {path: '/software/:id/edit', loader: preloadSoftwareEditPage(queryClient), lazy: {Component: loadSoftwareEditPage}, HydrateFallback: () => null},
    ], {initialEntries: [`/software/${id}/edit`]});
    renderWithQueries(<RouterProvider router={router}/>, queryClient);
}

describe('SoftwareEditPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('appears with the software in its form when the preload makes it in time', async () => {
        stubFetch(json(ELITE));

        renderRoute(ID);

        await screen.findByRole('heading', {level: 1, name: 'Edit software'});

        expect(screen.getByRole('textbox', {name: 'Title'})).toHaveValue('Elite');
        expect(screen.queryByTestId('software-editor-skeleton')).not.toBeInTheDocument();
    });

    it('returns to the software after saving', async () => {
        stubFetch(json(ELITE), noContent());
        renderRoute(ID);

        await userEvent.click(await screen.findByRole('button', {name: 'Save'}));

        await expect(screen.findByText('Software view')).resolves.toBeInTheDocument();
    });

    it('returns to the list after deleting', async () => {
        stubFetch(json(ELITE), noContent());
        renderRoute(ID);

        await userEvent.click(await screen.findByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await expect(screen.findByText('Software list')).resolves.toBeInTheDocument();
    });

});
