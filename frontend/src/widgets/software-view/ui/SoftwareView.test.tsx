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
const AUTHOR = '00000000-0000-0000-0009-000000000002';
const AUTHOR_LABEL = '00000000-0000-0000-0004-000000000001';
const GROUP = '00000000-0000-0000-0010-000000000002';
const GROUP_LABEL = '00000000-0000-0000-0004-000000000002';
const GROUP_CREDITS = [
    {id: AUTHOR_LABEL, type: 'author-label', title: 'J.P. — Jane Publisher', roles: ['publisher'], ownerId: AUTHOR},
    {id: GROUP, type: 'group', title: 'Solar Studio', roles: ['developer'], ownerId: GROUP},
    {id: GROUP_LABEL, type: 'group-label', title: 'Sol — Solar Studio', roles: ['developer', 'publisher'], ownerId: GROUP},
];
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
    credits: [],
    sources: [],
    added: {by: 'Zork', at: '2026-03-04T05:06:07Z'},
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
    added: {by: 'Zed', at: '2026-01-01T00:00:00Z'},
    canChange: true,
};

const MEMBER = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed', email: 'zed@example.com', groups: ['member'], abilities: ['add-entries']};

const SHOTS = [
    {id: '00000000-0000-0000-0055-000000000001', url: '/api/screenshots/00000000-0000-0000-0055-000000000001'},
    {id: '00000000-0000-0000-0055-000000000002', url: '/api/screenshots/00000000-0000-0000-0055-000000000002'},
];

/** Answers the details, the screenshots and the releases of the software, and who reads them. */
function stubFetch(details: () => Response, releases: () => Response = json([AY_MOD]), me: () => Response = json(MEMBER), screenshots: () => Response = json([])) {
    return stubFetchRoutes(new Map([
        [`/api/software/${ID}`, details],
        [`/api/software/${ID}/screenshots`, screenshots],
        [`/api/software/${ID}/releases`, releases],
        ['/api/auth/me', me],
    ]));
}

/** The labels of the details of the software: the releases table below has headers of its own. */
function detailLabels(text: string) {
    return screen.queryAllByText(text).filter((element) => element.closest('table') === null);
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
        expect(detailLabels('Release date')).toHaveLength(1);
        expect(screen.getByText('20.09.1984')).toBeInTheDocument();
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

    it('shows the screenshots of the software between its details and its releases', async () => {
        stubFetch(json(ELITE), json([AY_MOD]), json(MEMBER), json(SHOTS));
        renderView(ID);

        const screenshots = await screen.findByRole('region', {name: 'Screenshots'});

        expect(within(screenshots).getAllByRole('button', {name: /^Screenshot \d of 2$/u})).toHaveLength(2);

        const releases = screen.getByRole('region', {name: 'Releases'});

        expect(screenshots.compareDocumentPosition(releases) & Node.DOCUMENT_POSITION_FOLLOWING).toBe(Node.DOCUMENT_POSITION_FOLLOWING);
    });

    it('shows no screenshots section for software without screenshots', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        await screen.findByRole('region', {name: 'Releases'});

        expect(screen.queryByRole('region', {name: 'Screenshots'})).not.toBeInTheDocument();
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

    it('leads from the release date to the catalog of its year', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        await expect(screen.findByRole('link', {name: '20.09.1984'})).resolves.toHaveAttribute('href', '/software?year=1984');
    });

    it('leads from every category of a branch to the catalog of that category', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        const categories = await screen.findByRole('region', {name: 'Categories'});

        expect(within(categories).getAllByRole('link').map((link) => [link.textContent, link.getAttribute('href')])).toStrictEqual([
            ['Games', `/software/category/${GAMES}`],
            ['Action', `/software/category/${ACTION}`],
            ['Shooters', `/software/category/${SHOOTERS}`],
            ['Games', `/software/category/${GAMES}`],
            ['Strategy', `/software/category/${STRATEGY}`],
        ]);
    });

    it('leads from a credited label of an author to the label\'s page', async () => {
        stubFetch(json({
            ...ELITE,
            credits: [{id: AUTHOR_LABEL, type: 'author-label', title: 'J.D. — Jane Doe', roles: ['code'], ownerId: AUTHOR}],
        }));
        renderView(ID);

        const credits = await screen.findByRole('list', {name: 'Credits'});

        expect(within(credits).getByRole('link', {name: 'J.D. — Jane Doe'})).toHaveAttribute('href', `/labels/${AUTHOR_LABEL}`);
    });

    it('leads from the groups among the credits to the group pages and from every label to its page', async () => {
        stubFetch(json({...ELITE, credits: GROUP_CREDITS}));
        renderView(ID);

        const credits = await screen.findByRole('list', {name: 'Credits'});

        expect(within(credits).getByRole('link', {name: 'J.P. — Jane Publisher'})).toHaveAttribute('href', `/labels/${AUTHOR_LABEL}`);
        expect(within(credits).getByRole('link', {name: 'Solar Studio'})).toHaveAttribute('href', `/groups/${GROUP}`);
        expect(within(credits).getByRole('link', {name: 'Sol — Solar Studio'})).toHaveAttribute('href', `/labels/${GROUP_LABEL}`);
    });

    it('names the developers and publishers among the credits by their roles', async () => {
        stubFetch(json({...ELITE, credits: GROUP_CREDITS}));
        renderView(ID);

        const credits = await screen.findByRole('list', {name: 'Credits'});

        expect(within(credits).getAllByRole('listitem').map((item) => item.textContent)).toStrictEqual([
            'J.P. — Jane PublisherPublisher',
            'Solar StudioDeveloper',
            'Sol — Solar StudioDeveloperPublisher',
        ]);
    });

    it('leaves the unknown role out of the credits', async () => {
        stubFetch(json({...ELITE, credits: [
            {id: AUTHOR, type: 'author', title: 'Jane Doe', roles: ['unknown'], ownerId: AUTHOR},
            {id: GROUP, type: 'group', title: 'Solar Studio', roles: ['developer'], ownerId: GROUP},
        ]}));
        renderView(ID);

        const credits = await screen.findByRole('list', {name: 'Credits'});

        expect(within(credits).getAllByRole('listitem').map((item) => item.textContent)).toStrictEqual(['Jane Doe', 'Solar StudioDeveloper']);
    });

    it('leads from a credited label of nobody to the page of the label', async () => {
        stubFetch(json({...ELITE, credits: [{id: AUTHOR_LABEL, type: 'author-label', title: 'Ghost', roles: ['code'], ownerId: null}]}));
        renderView(ID);

        const credits = await screen.findByRole('list', {name: 'Credits'});

        expect(within(credits).getByRole('link', {name: 'Ghost'})).toHaveAttribute('href', `/labels/${AUTHOR_LABEL}`);
    });

    it('shows no categories for software in none', async () => {
        stubFetch(json({...ELITE, categories: []}));
        renderView(ID);

        await screen.findByRole('heading', {level: 1, name: 'Elite'});

        expect(screen.queryByRole('region', {name: 'Categories'})).not.toBeInTheDocument();
    });

    it('leads to the page of the software in the archive it was imported from', async () => {
        const url = 'https://vektor-06c.ru/programmnoe-obespechenie/arkadnye-igry/elite/';
        stubFetch(json({...ELITE, sources: [{archive: 'vektor-06c.ru', url}]}));
        renderView(ID);

        const sources = await screen.findByRole('region', {name: 'Source'});

        expect(within(sources).getByRole('link', {name: 'vektor-06c.ru'})).toHaveAttribute('href', url);
    });

    it('shows no source for software typed in by hand', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        await screen.findByRole('heading', {level: 1, name: 'Elite'});

        expect(screen.queryByRole('region', {name: 'Source'})).not.toBeInTheDocument();
    });

    it('shows nothing for an unknown description and release date', async () => {
        stubFetch(json({...ELITE, description: null, releaseDate: null}));
        renderView(ID);

        await screen.findByRole('heading', {level: 1, name: 'Elite'});

        expect(detailLabels('Release date')).toHaveLength(0);
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

    it('tells who added the software and when', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        await expect(screen.findByText('Added by Zork on 4 March 2026')).resolves.toBeInTheDocument();
    });

    it('tells only when the software was added once the account of whoever added it is gone', async () => {
        stubFetch(json({...ELITE, added: {by: null, at: '2026-03-04T05:06:07Z'}}));
        renderView(ID);

        await expect(screen.findByText('Added on 4 March 2026')).resolves.toBeInTheDocument();
    });
});
