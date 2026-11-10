import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {loadReleaseNewPage, preloadReleaseNewPage} from '../index';

const ID = '00000000-0000-0000-0001-000000000001';
const ELITE = {id: ID, title: 'Elite', description: null, releaseDate: null, platform: {code: 'vector', name: 'Vector'}, hardware: []};
const AY = {code: 'ay8910', name: 'AY-3-8910', group: 'sound'};

function renderRoute() {
    const queryClient = createQueryClient();
    const router = createMemoryRouter([
        {path: '/software/:id', element: <p>Software view</p>},
        {path: '/software/:id/releases/new', loader: preloadReleaseNewPage(queryClient), lazy: {Component: loadReleaseNewPage}, HydrateFallback: () => null},
    ], {initialEntries: [`/software/${ID}/releases/new`]});
    renderWithQueries(<RouterProvider router={router}/>, queryClient);
}

function answer() {
    return stubFetchRoutes(new Map([
        [`/api/software/${ID}`, json(ELITE)],
        ['/api/hardware?platform=vector', json([AY])],
        [`/api/software/${ID}/releases`, json({id: '00000000-0000-0000-0007-000000000002'}, 201)],
    ]));
}

describe('ReleaseNewPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('appears with the form and the hardware of the platform when the preload makes it in time', async () => {
        answer();

        renderRoute();

        await expect(screen.findByRole('heading', {level: 1, name: 'Add release to Elite'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('checkbox', {name: 'AY-3-8910'})).not.toBeChecked();
        expect(screen.queryByTestId('release-editor-skeleton')).not.toBeInTheDocument();
    });

    it('returns to the software after adding', async () => {
        answer();
        renderRoute();

        await userEvent.click(await screen.findByRole('button', {name: 'Add release'}));

        await expect(screen.findByText('Software view')).resolves.toBeInTheDocument();
    });
});
