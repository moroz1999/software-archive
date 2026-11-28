import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createQueryClient} from '@/shared/api';
import {json, noContent, stubFetchRoutes} from '@/shared/lib/test';
import {App} from './App';
import {type AppRouter, createAppRouter} from './router';

const SOFTWARE_ID = '00000000-0000-0000-0001-000000000001';
const RELEASE_ID = '00000000-0000-0000-0007-000000000001';
const SCREENSHOT_ID = '00000000-0000-0000-0055-000000000001';
const ARCHIVE_URL = `/api/software/${SOFTWARE_ID}/releases/${RELEASE_ID}/archive`;

const SOFTWARE = {
    id: SOFTWARE_ID,
    title: 'Elite',
    description: null,
    releaseDate: '1984-09-20',
    platform: {code: 'vector', name: 'Vector'},
    hardware: [],
    categories: [],
    credits: [],
    sources: [], links: [], preservation: 'unknown', instructions: null, lineage: {kind: null, reliability: null, rework: [], source: null}, compilationItems: [], compilations: [],
    origins: [], added: {by: 'Zed', at: '2026-01-01T00:00:00Z'},
    canChange: true,
};

const RELEASE = {
    id: RELEASE_ID,
    title: 'Elite AY',
    version: '1.1',
    type: 'mod',
    releaseDate: '1985-10-05',
    description: 'AY music\nwith improved controls',
    hardware: [
        {code: 'vector06c', name: 'Vector-06C', group: 'computers', groupName: 'Computers'},
        {code: 'ay8910', name: 'AY-3-8910', group: 'sound', groupName: 'Sound'},
    ],
    screenshots: [{id: SCREENSHOT_ID, url: `/api/screenshots/${SCREENSHOT_ID}`}],
    archive: {fileName: 'elite-ay.zip', size: 1536, url: ARCHIVE_URL},
    credits: [],
    added: {by: 'Zed', at: '2026-01-01T00:00:00Z'},
    canChange: true,
};

describe('release view routes', () => {
    let router: AppRouter | undefined;

    afterEach(() => {
        router?.dispose();
        router = undefined;
        window.history.replaceState(null, '', '/');
        vi.unstubAllGlobals();
    });

    function renderAt(path: string, releases: readonly unknown[] = [RELEASE], releaseResponse: (() => Response) | null = null) {
        stubFetchRoutes(new Map([
            ['/api/auth/me', noContent()],
            ['/api/platforms', json([{code: 'vector', name: 'Vector'}])],
            [`/api/software/${SOFTWARE_ID}`, json(SOFTWARE)],
            [`/api/software/${SOFTWARE_ID}/releases`, releaseResponse ?? json(releases)],
            [`/api/software/${SOFTWARE_ID}/releases/${RELEASE_ID}/screenshots`, json(RELEASE.screenshots)],
        ]));
        window.history.replaceState(null, '', path);
        const queryClient = createQueryClient();
        router = createAppRouter(queryClient);
        render(<App router={router} queryClient={queryClient}/>);
    }

    it('opens a release from its software and shows its main facts', async () => {
        renderAt(`/software/${SOFTWARE_ID}`);

        const releases = await screen.findByRole('region', {name: 'Releases'});
        await userEvent.click(await within(releases).findByRole('link', {name: 'Elite AY'}));

        await expect(screen.findByRole('heading', {level: 1, name: 'Elite AY'})).resolves.toBeInTheDocument();
        // The breadcrumbs and the page itself both lead back to the software.
        expect(screen.getAllByRole('link', {name: 'Elite'}).map((link) => link.getAttribute('href'))).toStrictEqual([`/software/${SOFTWARE_ID}`, `/software/${SOFTWARE_ID}`]);
        expect(screen.getByText('05.10.1985')).toBeInTheDocument();
        expect(screen.getByText('1.1')).toBeInTheDocument();
        expect(screen.getByText('Mod')).toBeInTheDocument();
    });

    it('shows the release description and effective hardware', async () => {
        renderAt(`/software/${SOFTWARE_ID}/releases/${RELEASE_ID}`);

        await screen.findByRole('heading', {level: 1, name: 'Elite AY'});

        expect(screen.getByText('AY music')).toBeInTheDocument();
        expect(screen.getByText('with improved controls')).toBeInTheDocument();

        const hardware = screen.getByRole('region', {name: 'Hardware'});

        expect(within(hardware).getByText('Vector-06C')).toBeInTheDocument();
        expect(within(hardware).getByText('AY-3-8910')).toBeInTheDocument();
    });

    it('shows the release screenshots and editing link', async () => {
        renderAt(`/software/${SOFTWARE_ID}/releases/${RELEASE_ID}`);

        await screen.findByRole('heading', {level: 1, name: 'Elite AY'});

        const screenshots = await screen.findByRole('region', {name: 'Screenshots'});

        expect(within(screenshots).getByRole('button', {name: 'Screenshot 1 of 1'})).toBeInTheDocument();

        await userEvent.click(screen.getByRole('button', {name: 'Actions'}));

        expect(screen.getByRole('link', {name: 'Edit'})).toHaveAttribute('href', `/software/${SOFTWARE_ID}/releases/${RELEASE_ID}/edit`);
    });

    it('offers the release archive by its original name and size', async () => {
        renderAt(`/software/${SOFTWARE_ID}/releases/${RELEASE_ID}`);

        await screen.findByRole('heading', {level: 1, name: 'Elite AY'});

        expect(screen.getByText('elite-ay.zip')).toBeInTheDocument();
        expect(screen.getByText('1.5 KB')).toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Download archive'})).toHaveAttribute('href', ARCHIVE_URL);
    });

    it('does not offer editing when the release may not be changed', async () => {
        renderAt(`/software/${SOFTWARE_ID}/releases/${RELEASE_ID}`, [{...RELEASE, canChange: false}]);

        await screen.findByRole('heading', {level: 1, name: 'Elite AY'});

        expect(screen.queryByRole('button', {name: 'Actions'})).not.toBeInTheDocument();
    });

    it('shows the not found page when the software has no such release', async () => {
        renderAt(`/software/${SOFTWARE_ID}/releases/${RELEASE_ID}`, []);

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
        expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
    });

    it('shows the not found page for a malformed release id without asking the backend', async () => {
        const fetchMock = stubFetchRoutes(new Map([
            ['/api/auth/me', noContent()],
            ['/api/platforms', json([{code: 'vector', name: 'Vector'}])],
        ]));
        window.history.replaceState(null, '', `/software/${SOFTWARE_ID}/releases/42`);
        const queryClient = createQueryClient();
        router = createAppRouter(queryClient);
        render(<App router={router} queryClient={queryClient}/>);

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
        expect(fetchMock).not.toHaveBeenCalledWith(expect.stringContaining('/api/software/'), expect.anything());
    });

    it('hands a loading failure to the system error dialog', async () => {
        renderAt(`/software/${SOFTWARE_ID}/releases/${RELEASE_ID}`, [RELEASE], json({status: 500}, 500));

        await expect(screen.findByRole('alertdialog', {name: 'Something went wrong'})).resolves.toBeInTheDocument();
        expect(screen.queryByRole('heading', {level: 1, name: 'Elite AY'})).not.toBeInTheDocument();
    });
});
