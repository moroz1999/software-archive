import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {createQueryClient} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {loadReleaseEditPage, preloadReleaseEditPage} from '../index';

const ID = '00000000-0000-0000-0001-000000000001';
const RELEASE = '00000000-0000-0000-0007-000000000002';
const ELITE = {id: ID, title: 'Elite', description: null, releaseDate: null, platform: {code: 'vector', name: 'Vector'}, hardware: [], categories: [], credits: [], sources: [], preservation: 'unknown', added: {by: 'Zed', at: '2026-01-01T00:00:00Z'}, canChange: true};
const AY = {code: 'ay8910', name: 'AY-3-8910', group: 'sound'};
const SHOT = {id: '00000000-0000-0000-0055-000000000001', url: '/api/screenshots/00000000-0000-0000-0055-000000000001'};
const ARCHIVE = {fileName: 'elite-ay.zip', size: 1536, url: `/api/software/${ID}/releases/${RELEASE}/archive`};
const AY_MOD = {id: RELEASE, title: 'Elite AY', version: '1.1', type: 'mod', releaseDate: null, description: null, hardware: [AY], archive: ARCHIVE};

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
        [`/api/software/${ID}/releases/${RELEASE}/screenshots`, json([SHOT])],
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

    it('offers deleting next to the page title and leads back to the release from the form', async () => {
        answer();

        renderRoute();

        const heading = await screen.findByRole('heading', {level: 1, name: 'Edit release of Elite'});
        await screen.findByRole('textbox', {name: 'Version'});

        // The title's line comes before the form: its Delete is the page's first button.
        expect(screen.getAllByRole('button').at(0)).toHaveAccessibleName('Delete');
        expect(heading).toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Cancel'})).toHaveAttribute('href', `/software/${ID}/releases/${RELEASE}`);
    });

    it('offers the screenshots of the release to change below its form', async () => {
        answer();

        renderRoute();

        const screenshots = await screen.findByRole('list', {name: 'Screenshots'});

        expect(within(screenshots).getByRole('img', {name: 'Screenshot 1'})).toHaveAttribute('src', SHOT.url);
    });

    it('offers the current archive for replacement or removal', async () => {
        answer();

        renderRoute();

        await expect(screen.findByText('elite-ay.zip')).resolves.toBeInTheDocument();
        expect(screen.getByText('1.5 KB')).toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Replace archive'})).toBeEnabled();
        expect(screen.getByRole('button', {name: 'Remove archive'})).toBeEnabled();
    });

    it('replaces the archive together with the release changes', async () => {
        const fetchMock = answer();
        const original = fetchMock.getMockImplementation();
        fetchMock.mockImplementation((input, init) => (init?.method === 'PUT'
            ? Promise.resolve(noContent()())
            : original === undefined ? Promise.reject(new Error('no stub')) : original(input, init)));
        const archive = new File(['replacement'], 'elite-ay-v2.zip', {type: 'application/zip'});
        renderRoute();

        await userEvent.upload(await screen.findByLabelText('Replace archive'), archive);
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await expect(screen.findByText('Software view')).resolves.toBeInTheDocument();

        const request = fetchMock.mock.calls.find(([, init]) => init?.method === 'PUT');
        const body = request?.[1]?.body;

        expect(body).toBeInstanceOf(FormData);
        expect(body instanceof FormData ? body.get('archive') : null).toBe(archive);
    });

    it('removes the archive together with the release changes', async () => {
        const fetchMock = answer();
        const original = fetchMock.getMockImplementation();
        fetchMock.mockImplementation((input, init) => (init?.method === 'PUT'
            ? Promise.resolve(noContent()())
            : original === undefined ? Promise.reject(new Error('no stub')) : original(input, init)));
        renderRoute();

        await userEvent.click(await screen.findByRole('button', {name: 'Remove archive'}));
        await userEvent.click(screen.getByRole('button', {name: 'Save'}));

        await expect(screen.findByText('Software view')).resolves.toBeInTheDocument();

        const request = fetchMock.mock.calls.find(([, init]) => init?.method === 'PUT');
        const body = request?.[1]?.body;

        expect(body).toBeInstanceOf(FormData);
        expect(body instanceof FormData ? body.get('removeArchive') : null).toBe('true');
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
