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

const SOFTWARE = {
    id: SOFTWARE_ID,
    title: 'Elite',
    description: null,
    releaseDate: '1984-09-20',
    platform: {code: 'vector', name: 'Vector'},
    hardware: [],
    categories: [],
    publishers: [],
    developers: [],
    credits: [],
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
        {code: 'vector06c', name: 'Vector-06C', group: 'computers'},
        {code: 'ay8910', name: 'AY-3-8910', group: 'sound'},
    ],
    screenshots: [{id: SCREENSHOT_ID, url: `/api/screenshots/${SCREENSHOT_ID}`}],
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
        expect(screen.getByRole('link', {name: 'Elite'})).toHaveAttribute('href', `/software/${SOFTWARE_ID}`);
        expect(screen.getByText('5 October 1985')).toBeInTheDocument();
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
        expect(screen.getByRole('link', {name: 'Edit'})).toHaveAttribute('href', `/software/${SOFTWARE_ID}/releases/${RELEASE_ID}/edit`);
    });

    it('does not offer editing when the release may not be changed', async () => {
        renderAt(`/software/${SOFTWARE_ID}/releases/${RELEASE_ID}`, [{...RELEASE, canChange: false}]);

        await screen.findByRole('heading', {level: 1, name: 'Elite AY'});

        expect(screen.queryByRole('link', {name: 'Edit'})).not.toBeInTheDocument();
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
