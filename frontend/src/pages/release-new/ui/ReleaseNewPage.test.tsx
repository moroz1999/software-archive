import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {loadReleaseNewPage, preloadReleaseNewPage} from '../index';

const ID = '00000000-0000-0000-0001-000000000001';
const ELITE = {id: ID, title: 'Elite', description: null, releaseDate: null, platform: {code: 'vector', name: 'Vector'}, hardware: [], categories: [], credits: [], sources: [], canChange: true};
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

    it('adds one archive together with the release', async () => {
        const fetchMock = answer();
        const archive = new File(['archive'], 'elite.zip', {type: 'application/zip'});
        renderRoute();

        await userEvent.upload(await screen.findByLabelText('Add archive'), archive);
        await userEvent.click(screen.getByRole('button', {name: 'Add release'}));

        await expect(screen.findByText('Software view')).resolves.toBeInTheDocument();

        const request = fetchMock.mock.calls.find(([, init]) => init?.method === 'POST');
        const body = request?.[1]?.body;

        expect(body).toBeInstanceOf(FormData);
        expect(body instanceof FormData ? body.get('archive') : null).toBe(archive);
    });

    it('refuses an archive larger than 100 MB before adding the release', async () => {
        const fetchMock = answer();
        const archive = new File(['archive'], 'huge.zip', {type: 'application/zip'});
        Object.defineProperty(archive, 'size', {value: 100 * 1024 * 1024 + 1});
        renderRoute();

        await userEvent.upload(await screen.findByLabelText('Add archive'), archive);

        expect(screen.getByText('A release archive can be at most 100 MB.')).toBeInTheDocument();
        expect(fetchMock.mock.calls.some(([, init]) => init?.method === 'POST')).toBe(false);
    });
});
