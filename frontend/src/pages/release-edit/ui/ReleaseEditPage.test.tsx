import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {loadReleaseEditPage, preloadReleaseEditPage} from '../index';

const ID = '00000000-0000-0000-0001-000000000001';
const RELEASE = '00000000-0000-0000-0007-000000000002';
const ELITE = {id: ID, title: 'Elite', description: null, releaseDate: null, platform: {code: 'vector', name: 'Vector'}, hardware: [], categories: []};
const AY = {code: 'ay8910', name: 'AY-3-8910', group: 'sound'};
const AY_MOD = {id: RELEASE, title: 'Elite AY', version: '1.1', type: 'mod', releaseDate: null, description: null, hardware: [AY]};

function renderRoute() {
    const queryClient = createQueryClient();
    const router = createMemoryRouter([
        {path: '/software/:id', element: <p>Software view</p>},
        {path: '/software/:id/releases/:releaseId/edit', loader: preloadReleaseEditPage(queryClient), lazy: {Component: loadReleaseEditPage}, HydrateFallback: () => null},
    ], {initialEntries: [`/software/${ID}/releases/${RELEASE}/edit`]});
    renderWithQueries(<RouterProvider router={router}/>, queryClient);
}

function answer() {
    return stubFetchRoutes(new Map([
        [`/api/software/${ID}`, json(ELITE)],
        ['/api/hardware?platform=vector', json([AY])],
        [`/api/software/${ID}/releases/${RELEASE}`, json(AY_MOD)],
    ]));
}

describe('ReleaseEditPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('appears with the release in its form when the preload makes it in time', async () => {
        answer();

        renderRoute();

        await expect(screen.findByRole('heading', {level: 1, name: 'Edit release of Elite'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('textbox', {name: 'Version'})).toHaveValue('1.1');
        expect(screen.getByRole('checkbox', {name: 'AY-3-8910'})).toBeChecked();
        expect(screen.queryByTestId('release-editor-skeleton')).not.toBeInTheDocument();
    });

    it('returns to the software after saving', async () => {
        const fetchMock = answer();
        const original = fetchMock.getMockImplementation();
        fetchMock.mockImplementation((input, init) => (init?.method === 'PUT'
            ? Promise.resolve(noContent()())
            : original === undefined ? Promise.reject(new Error('no stub')) : original(input, init)));
        renderRoute();

        await userEvent.click(await screen.findByRole('button', {name: 'Save'}));

        await expect(screen.findByText('Software view')).resolves.toBeInTheDocument();
    });

    it('returns to the software after deleting', async () => {
        const fetchMock = answer();
        const original = fetchMock.getMockImplementation();
        fetchMock.mockImplementation((input, init) => (init?.method === 'DELETE'
            ? Promise.resolve(noContent()())
            : original === undefined ? Promise.reject(new Error('no stub')) : original(input, init)));
        renderRoute();

        await userEvent.click(await screen.findByRole('button', {name: 'Delete'}));
        await userEvent.click(screen.getByRole('button', {name: 'Delete for good'}));

        await expect(screen.findByText('Software view')).resolves.toBeInTheDocument();
    });
});
