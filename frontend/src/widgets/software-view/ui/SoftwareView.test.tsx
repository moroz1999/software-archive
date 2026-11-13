import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {toSoftwareId} from '@/entities/software';
import {CurrentUserProvider} from '@/entities/users';
import {SoftwareView} from './SoftwareView';

const ID = '00000000-0000-0000-0001-000000000001';
const VECTOR06C = {code: 'vector06c', name: 'Vector-06C', group: 'computers'};
const BEEPER = {code: 'beeper', name: 'Beeper', group: 'sound'};
const GAMES = '00000000-0000-0000-0006-000000000001';
const ACTION = '00000000-0000-0000-0006-000000000002';
const SHOOTERS = '00000000-0000-0000-0006-000000000003';
const STRATEGY = '00000000-0000-0000-0006-000000000004';
const ELITE = {
    id: ID,
    title: 'Elite',
    description: 'Space trading\nand combat',
    releaseDate: '1984-09-20',
    platform: {code: 'vector', name: 'Vector'},
    hardware: [VECTOR06C, BEEPER],
    // Games → Action → Shooters and Games → Strategy, in the order of the tree.
    categories: [
        {id: SHOOTERS, title: 'Shooters', branch: [{id: GAMES, title: 'Games'}, {id: ACTION, title: 'Action'}, {id: SHOOTERS, title: 'Shooters'}]},
        {id: STRATEGY, title: 'Strategy', branch: [{id: GAMES, title: 'Games'}, {id: STRATEGY, title: 'Strategy'}]},
    ],
    publishers: [],
    developers: [],
    credits: [],
    canChange: true,
};
const AY_MOD = {
    id: '00000000-0000-0000-0007-000000000001',
    title: 'Elite AY',
    version: '1.1',
    type: 'mod',
    releaseDate: null,
    description: null,
    hardware: [VECTOR06C, {code: 'ay8910', name: 'AY-3-8910', group: 'sound'}],
    canChange: true,
};

const MEMBER = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed', email: 'zed@example.com', groups: ['member'], abilities: ['add-entries']};

/** Answers the details and the releases of the software, and who reads them. */
function stubFetch(details: () => Response, releases: () => Response = json([AY_MOD]), me: () => Response = json(MEMBER)) {
    return stubFetchRoutes(new Map([[`/api/software/${ID}`, details], [`/api/software/${ID}/releases`, releases], ['/api/auth/me', me]]));
}

function renderView(id: string) {
    renderWithQueries(<CurrentUserProvider><MemoryRouter><SoftwareView id={toSoftwareId(id)}/></MemoryRouter></CurrentUserProvider>);
}

describe(SoftwareView, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('shows a skeleton until the software arrives', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        expect(screen.getByTestId('software-view-skeleton')).toBeInTheDocument();
        await expect(screen.findByRole('heading', {level: 1, name: 'Elite'})).resolves.toBeInTheDocument();
        expect(screen.queryByTestId('software-view-skeleton')).not.toBeInTheDocument();
    });

    it('shows the software under its title, its description line by line', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        await expect(screen.findByRole('heading', {level: 1, name: 'Elite'})).resolves.toBeInTheDocument();
        expect(screen.getByText('20 September 1984')).toBeInTheDocument();
        expect(screen.getByText('Space trading')).toBeInTheDocument();
        expect(screen.getByText('and combat')).toBeInTheDocument();
    });

    it('shows the platform and the hardware of the software by group', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        const details = await screen.findByRole('region', {name: 'Hardware'});

        expect(screen.getByText('Vector')).toBeInTheDocument();
        expect(within(within(details).getByRole('group', {name: 'Computers'})).getByText('Vector-06C')).toBeInTheDocument();
        expect(within(within(details).getByRole('group', {name: 'Sound'})).getByText('Beeper')).toBeInTheDocument();
    });

    it('shows the releases of the software with the hardware each one shows', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        const releases = await screen.findByRole('region', {name: 'Releases'});
        const release = await within(releases).findByRole('row', {name: /Elite AY/});

        expect(release).toHaveTextContent('AY-3-8910');
    });

    it('leads to adding a release and to the editing of each release', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        const releases = await screen.findByRole('region', {name: 'Releases'});
        const release = await within(releases).findByRole('row', {name: /Elite AY/});

        await expect(within(releases).findByRole('link', {name: 'Add release'})).resolves.toHaveAttribute('href', `/software/${ID}/releases/new`);
        expect(within(release).getByRole('link', {name: 'Edit Elite AY'})).toHaveAttribute('href', expect.stringMatching(new RegExp(`^/software/${ID}/releases/[0-9a-f-]+/edit$`)));
    });

    it('leads to the editing of the software', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        await expect(screen.findByRole('link', {name: 'Edit'})).resolves.toHaveAttribute('href', `/software/${ID}/edit`);
    });

    it('shows every category of the software with its whole branch from the top, in their order', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        const categories = await screen.findByRole('region', {name: 'Categories'});
        const branches = within(categories).getAllByRole('list', {name: /./u});

        expect(branches.map((branch) => branch.getAttribute('aria-label'))).toStrictEqual(['Games › Action › Shooters', 'Games › Strategy']);
        expect(branches.map((branch) => within(branch).getAllByRole('listitem').map((item) => item.textContent))).toStrictEqual([
            ['Games', 'Action', 'Shooters'],
            ['Games', 'Strategy'],
        ]);
    });

    it('shows no categories for software in none', async () => {
        stubFetch(json({...ELITE, categories: []}));
        renderView(ID);

        await screen.findByRole('heading', {level: 1, name: 'Elite'});

        expect(screen.queryByRole('region', {name: 'Categories'})).not.toBeInTheDocument();
    });

    it('shows nothing for an unknown description and release date', async () => {
        stubFetch(json({...ELITE, description: null, releaseDate: null}));
        renderView(ID);

        await screen.findByRole('heading', {level: 1, name: 'Elite'});

        expect(screen.queryByText('Released')).not.toBeInTheDocument();
    });

    it('hands a business error to the error dialog and shows nothing of its own', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetch(json({status: 409, code: 'software.conflict'}, 409));
        renderView(ID);

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(screen.queryByRole('alert')).not.toBeInTheDocument();
        expect(screen.queryByRole('button', {name: 'Retry'})).not.toBeInTheDocument();
    });

    it('hands a system error to the error dialog and shows nothing of its own', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetch(json({status: 500}, 500));
        renderView(ID);

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(reports[0]?.error.kind).toBe('server');
        expect(screen.queryByTestId('software-view-skeleton')).not.toBeInTheDocument();
        expect(screen.queryByRole('link', {name: 'Edit'})).not.toBeInTheDocument();
    });

    it('offers no editing of software the reader may not change, nor of its releases they may not change', async () => {
        stubFetch(json({...ELITE, canChange: false}), json([{...AY_MOD, canChange: false}]));
        renderView(ID);

        const releases = await screen.findByRole('region', {name: 'Releases'});
        await within(releases).findByRole('row', {name: /Elite AY/});
        await within(releases).findByRole('link', {name: 'Add release'});

        expect(screen.queryByRole('link', {name: 'Edit'})).not.toBeInTheDocument();
        expect(within(releases).queryByRole('link', {name: 'Edit Elite AY'})).not.toBeInTheDocument();
    });

    it('offers editing of a release the reader may change inside software they may not', async () => {
        stubFetch(json({...ELITE, canChange: false}));
        renderView(ID);

        const releases = await screen.findByRole('region', {name: 'Releases'});
        const release = await within(releases).findByRole('row', {name: /Elite AY/});

        expect(within(release).getByRole('link', {name: 'Edit Elite AY'})).toBeInTheDocument();
        expect(screen.queryByRole('link', {name: 'Edit'})).not.toBeInTheDocument();
    });

    it('offers a public visitor no adding of releases', async () => {
        stubFetch(json({...ELITE, canChange: false}), json([{...AY_MOD, canChange: false}]), noContent());
        renderView(ID);

        const releases = await screen.findByRole('region', {name: 'Releases'});
        await within(releases).findByRole('row', {name: /Elite AY/});

        await vi.waitFor(() => {
            expect(within(releases).queryByRole('link', {name: 'Add release'})).not.toBeInTheDocument();
        });
    });
});
