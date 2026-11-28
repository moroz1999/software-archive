import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, waitFor, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import i18n from 'i18next';
import {type components, subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {breadcrumbSteps, json, noContent, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {CurrentUserProvider} from '@/entities/users';
import {SoftwareView} from './SoftwareView';

const ID = '00000000-0000-0000-0001-000000000001';
const VECTOR06C = {code: 'vector06c', name: 'Vector-06C', group: 'computers', groupName: 'Computers'};
const BEEPER = {code: 'beeper', name: 'Beeper', group: 'sound', groupName: 'Sound'};
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
    sources: [], links: [],
    preservation: 'unknown',
    instructions: null,
    lineage: {kind: null, reliability: null, rework: [], source: null},
    compilationItems: [],
    compilations: [],
    origins: [], added: {by: 'Zork', at: '2026-03-04T05:06:07Z'},
    canChange: true,
} satisfies components['schemas']['SoftwareDetailsDto'];
const AY_MOD = {
    id: '00000000-0000-0000-0007-000000000001',
    title: 'Elite AY',
    version: '1.1',
    type: 'mod',
    releaseDate: null,
    description: null,
    hardware: [VECTOR06C, {code: 'ay8910', name: 'AY-3-8910', group: 'sound', groupName: 'Sound'}],
    screenshots: [],
    archive: null,
    credits: [],
    added: {by: 'Zed', at: '2026-01-01T00:00:00Z'},
    canChange: true,
} satisfies components['schemas']['SoftwareReleaseDto'];

const MEMBER = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed', email: 'zed@example.com', groups: ['member'], abilities: ['add-entries']};

const SHOTS = [
    {id: '00000000-0000-0000-0055-000000000001', url: '/api/screenshots/00000000-0000-0000-0055-000000000001'},
    {id: '00000000-0000-0000-0055-000000000002', url: '/api/screenshots/00000000-0000-0000-0055-000000000002'},
];

/** The pictures of the other galleries of the software, none unless given. */
interface Galleries {
    readonly maps?: () => Response;
    readonly covers?: () => Response;
    readonly adverts?: () => Response;
    readonly compilationPrograms?: () => Response;
    readonly rating?: () => Response;
    readonly votes?: () => Response;
    readonly vote?: () => Response;
}

const RATED = {rating: 4.0769, votesAmount: 3, myVote: null};

/**
 * Answers the details, the screenshots and the other galleries, the releases, the rating and the votes of the software,
 * and who reads them.
 */
function stubFetch(details: () => Response, releases: () => Response = json([AY_MOD]), me: () => Response = json(MEMBER), screenshots: () => Response = json([]), galleries: Galleries = {}) {
    return stubFetchRoutes(new Map([
        [`/api/software/${ID}`, details],
        [`/api/software/${ID}/screenshots`, screenshots],
        [`/api/software/${ID}/maps`, galleries.maps ?? json([])],
        [`/api/software/${ID}/covers`, galleries.covers ?? json([])],
        [`/api/software/${ID}/adverts`, galleries.adverts ?? json([])],
        [`/api/software/${ID}/releases`, releases],
        [`/api/software/${ID}/compilation-items`, galleries.compilationPrograms ?? json([])],
        [`/api/software/${ID}/rating`, galleries.rating ?? json(RATED)],
        [`/api/software/${ID}/votes`, galleries.votes ?? json([])],
        [`/api/software/${ID}/vote`, galleries.vote ?? json(RATED)],
        ['/api/auth/me', me],
    ]));
}

/** The labels of the details of the software: the releases table below has headers of its own. */
function detailLabels(text: string) {
    return screen.queryAllByText(text).filter((element) => element.closest('table') === null);
}

function renderView(id: string) {
    renderWithQueries(<CurrentUserProvider><MemoryRouter><SoftwareView id={id}/></MemoryRouter></CurrentUserProvider>);
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

    it('shows the compilation as cards directly below releases and links to collections that include it', async () => {
        const program = {id: '00000000-0000-0000-0001-000000000002', title: 'Jetpac'};
        const compilation = {id: '00000000-0000-0000-0001-000000000003', title: 'Games of the Year'};
        stubFetch(json({...ELITE, compilationItems: [program], compilations: [compilation]}), undefined, noContent(), undefined, {
            compilationPrograms: json([{...program, releaseDate: '1983', screenshots: SHOTS, madeBy: ['Ultimate'], rating: 3.909, votesAmount: 1}]),
        });
        renderView(ID);

        const contents = within(await screen.findByRole('region', {name: 'Compilation programs'}));
        const card = await contents.findByRole('link', {name: program.title});
        const stars = await contents.findByRole('img', {name: 'Rating 3.91 of 5'});
        const collections = within(screen.getByRole('region', {name: 'Included in compilations'}));
        const headings = screen.getAllByRole('heading', {level: 2});
        const releases = screen.getByRole('heading', {level: 2, name: 'Releases'});
        const compilationHeading = contents.getByRole('heading', {level: 2, name: 'Compilation programs'});

        expect(headings.indexOf(compilationHeading)).toBe(headings.indexOf(releases) + 1);
        expect(card).toHaveAttribute('href', `/software/${program.id}`);
        expect(within(card).getByRole('img', {name: program.title})).toHaveAttribute('src', SHOTS[0]?.url);
        expect(card).not.toContainElement(stars);
        expect(collections.getByRole('link', {name: compilation.title})).toHaveAttribute('href', `/software/${compilation.id}`);
    });

    it('leaves out empty compilation sections', async () => {
        stubFetch(json({...ELITE, compilationItems: [], compilations: []}));
        renderView(ID);

        await screen.findByRole('heading', {level: 1, name: 'Elite'});

        expect(screen.queryByRole('region', {name: 'Compilation programs'})).not.toBeInTheDocument();
        expect(screen.queryByRole('region', {name: 'Included in compilations'})).not.toBeInTheDocument();
    });

    it('names the browser tab after the software and its platform', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        await expect(screen.findByRole('heading', {level: 1, name: 'Elite'})).resolves.toBeInTheDocument();
        expect(document.title).toBe('Elite (Vector) — Ретротека');
    });

    it('leads back from the software through the catalog and the branch of its first category', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        await screen.findByRole('heading', {level: 1, name: 'Elite'});

        expect(breadcrumbSteps()).toStrictEqual([
            ['Software', '/software'],
            ['Games', `/software/category/${GAMES}`],
            ['Action', `/software/category/${ACTION}`],
            ['Shooters', `/software/category/${SHOOTERS}`],
            ['Elite', null],
        ]);
    });

    it('leads back from software in no category straight to the catalog', async () => {
        stubFetch(json({...ELITE, categories: []}));
        renderView(ID);

        await screen.findByRole('heading', {level: 1, name: 'Elite'});

        expect(breadcrumbSteps()).toStrictEqual([['Software', '/software'], ['Elite', null]]);
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

        await expect(within(screenshots).findAllByRole('button', {name: /^Screenshot \d of 2$/u})).resolves.toHaveLength(2);

        const releases = screen.getByRole('region', {name: 'Releases'});

        expect(screenshots.compareDocumentPosition(releases) & Node.DOCUMENT_POSITION_FOLLOWING).toBe(Node.DOCUMENT_POSITION_FOLLOWING);
    });

    it('shows no screenshots section for software without screenshots', async () => {
        stubFetch(json(ELITE), json([AY_MOD]), json(MEMBER), json([]));
        renderView(ID);

        await screen.findByRole('region', {name: 'Releases'});

        await waitFor(() => {
            expect(screen.queryByRole('region', {name: 'Screenshots'})).not.toBeInTheDocument();
        });
    });

    it('shows the maps, the covers and the adverts of the software, each in a gallery of its own after the screenshots', async () => {
        stubFetch(json(ELITE), json([AY_MOD]), json(MEMBER), json(SHOTS), {maps: json(SHOTS), covers: json([SHOTS[0]])});
        renderView(ID);

        const maps = await screen.findByRole('region', {name: 'Maps'});

        await expect(within(maps).findAllByRole('button', {name: /^Map \d of 2$/u})).resolves.toHaveLength(2);
        await expect(screen.findByRole('region', {name: 'Covers'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('region', {name: 'Screenshots'}).compareDocumentPosition(maps) & Node.DOCUMENT_POSITION_FOLLOWING).toBe(Node.DOCUMENT_POSITION_FOLLOWING);
        expect(screen.queryByRole('region', {name: 'Adverts'})).not.toBeInTheDocument();
    });

    it('lets whoever may change the software move each picture of a gallery a place', async () => {
        stubFetch(json(ELITE), json([AY_MOD]), json(MEMBER), json([]), {adverts: json(SHOTS)});
        renderView(ID);

        const adverts = await screen.findByRole('region', {name: 'Adverts'});

        await expect(within(adverts).findByRole('button', {name: 'Move advert 1 right'})).resolves.toBeEnabled();
        expect(within(adverts).getByRole('button', {name: 'Move advert 2 right'})).toBeDisabled();
    });

    it('shows the instructions of the software line by line, apart from its description', async () => {
        stubFetch(json({...ELITE, instructions: 'LOAD ""\nPress Space to start'}));
        renderView(ID);

        const instructions = await screen.findByRole('region', {name: 'Instructions'});

        expect(within(instructions).getByText('LOAD ""')).toBeInTheDocument();
        expect(within(instructions).getByText('Press Space to start')).toBeInTheDocument();
        expect(within(instructions).queryByText('Space trading')).not.toBeInTheDocument();
    });

    it('shows no instructions section for software without instructions', async () => {
        stubFetch(json({...ELITE, instructions: null}));
        renderView(ID);

        await expect(screen.findByRole('heading', {level: 1, name: 'Elite'})).resolves.toBeInTheDocument();
        expect(screen.queryByRole('region', {name: 'Instructions'})).not.toBeInTheDocument();
    });

    it('lets whoever may change the software move each screenshot a place, never past the ends', async () => {
        stubFetch(json(ELITE), json([AY_MOD]), json(MEMBER), json(SHOTS));
        renderView(ID);

        const screenshots = await screen.findByRole('region', {name: 'Screenshots'});

        await expect(within(screenshots).findByRole('button', {name: 'Move screenshot 1 left'})).resolves.toBeDisabled();
        expect(within(screenshots).getByRole('button', {name: 'Move screenshot 1 right'})).toBeEnabled();
        expect(within(screenshots).getByRole('button', {name: 'Move screenshot 2 left'})).toBeEnabled();
        expect(within(screenshots).getByRole('button', {name: 'Move screenshot 2 right'})).toBeDisabled();
    });

    it('offers no moving of screenshots to whoever may not change the software', async () => {
        stubFetch(json({...ELITE, canChange: false}), json([AY_MOD]), json(MEMBER), json(SHOTS));
        renderView(ID);

        const screenshots = await screen.findByRole('region', {name: 'Screenshots'});
        await within(screenshots).findAllByRole('button', {name: /^Screenshot \d of 2$/u});

        expect(within(screenshots).queryByRole('button', {name: /^Move screenshot/u})).not.toBeInTheDocument();
    });

    it('moves a screenshot by sending every screenshot in the new order and shows them so', async () => {
        const [first, second] = SHOTS;
        let shown = SHOTS;
        const fetchMock = stubFetchRoutes(new Map([
            [`/api/software/${ID}`, json(ELITE)],
            [`/api/software/${ID}/screenshots`, () => json(shown)()],
            [`/api/software/${ID}/screenshots/order`, () => {
                shown = [second, first].filter((shot) => shot !== undefined);
                return noContent()();
            }],
            [`/api/software/${ID}/releases`, json([AY_MOD])],
            ['/api/auth/me', json(MEMBER)],
        ]));
        renderView(ID);

        const screenshots = await screen.findByRole('region', {name: 'Screenshots'});
        await userEvent.click(await within(screenshots).findByRole('button', {name: 'Move screenshot 1 right'}));

        await waitFor(() => {
            expect(within(screenshots).getAllByRole('img', {hidden: true}).map((picture) => picture.getAttribute('src'))).toStrictEqual([second?.url, first?.url]);
        });

        const put = fetchMock.mock.calls.find(([url]) => url === `/api/software/${ID}/screenshots/order`);

        expect(put?.[1]?.method).toBe('PUT');
        expect(put?.[1]?.body).toBe(JSON.stringify({order: [second?.id, first?.id]}));
    });

    it('shows next to the screenshots why a move was refused', async () => {
        stubFetchRoutes(new Map([
            [`/api/software/${ID}`, json(ELITE)],
            [`/api/software/${ID}/screenshots`, json(SHOTS)],
            [`/api/software/${ID}/screenshots/order`, json({status: 403, code: 'access.denied'}, 403)],
            [`/api/software/${ID}/releases`, json([AY_MOD])],
            ['/api/auth/me', json(MEMBER)],
        ]));
        renderView(ID);

        const screenshots = await screen.findByRole('region', {name: 'Screenshots'});
        await userEvent.click(await within(screenshots).findByRole('button', {name: 'Move screenshot 2 left'}));

        await expect(within(screenshots).findByRole('alert')).resolves.toHaveTextContent('Only whoever added this, or an administrator, may change it.');
    });

    it('shows the screenshots as they are now when they changed before the move', async () => {
        const [first, second] = SHOTS;
        const third = {id: '00000000-0000-0000-0055-000000000003', url: '/api/screenshots/00000000-0000-0000-0055-000000000003'};
        let shown = SHOTS;
        stubFetchRoutes(new Map([
            [`/api/software/${ID}`, json(ELITE)],
            [`/api/software/${ID}/screenshots`, () => json(shown)()],
            [`/api/software/${ID}/screenshots/order`, () => {
                shown = [...SHOTS, third];
                return json({status: 409, code: 'screenshot.order-outdated'}, 409)();
            }],
            [`/api/software/${ID}/releases`, json([AY_MOD])],
            ['/api/auth/me', json(MEMBER)],
        ]));
        renderView(ID);

        const screenshots = await screen.findByRole('region', {name: 'Screenshots'});
        await userEvent.click(await within(screenshots).findByRole('button', {name: 'Move screenshot 1 right'}));

        await expect(within(screenshots).findByRole('alert')).resolves.toHaveTextContent('The screenshots have changed meanwhile; here they are as they are now.');

        await waitFor(() => {
            expect(within(screenshots).getAllByRole('img', {hidden: true}).map((picture) => picture.getAttribute('src'))).toStrictEqual([first?.url, second?.url, third.url]);
        });
    });

    it('shows the releases of the software with the hardware each one shows', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        const releases = await screen.findByRole('region', {name: 'Releases'});
        const release = await within(releases).findByRole('row', {name: /Elite AY/});

        expect(release).toHaveTextContent('AY-3-8910');
    });

    it('shows who is credited on each release, leading to their pages', async () => {
        stubFetch(json(ELITE), json([{...AY_MOD, credits: GROUP_CREDITS}]));
        renderView(ID);

        const releases = await screen.findByRole('region', {name: 'Releases'});
        const release = await within(releases).findByRole('row', {name: /Elite AY/});

        expect(within(release).getByRole('link', {name: 'J.P. — Jane Publisher'})).toHaveAttribute('href', `/labels/${AUTHOR_LABEL}`);
        expect(within(release).getByRole('link', {name: 'Solar Studio'})).toHaveAttribute('href', `/groups/${GROUP}`);
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

        const menu = await screen.findByRole('button', {name: 'Actions'});

        expect(screen.queryByRole('link', {name: 'Edit'})).not.toBeInTheDocument();

        await userEvent.click(menu);

        expect(screen.getByRole('link', {name: 'Edit'})).toHaveAttribute('href', `/software/${ID}/edit`);
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

    it('names the authors and links in Russian', async () => {
        await i18n.changeLanguage('ru');
        stubFetch(json({
            ...ELITE,
            credits: [{id: AUTHOR_LABEL, type: 'author-label', title: 'J.D. — Jane Doe', roles: ['code'], ownerId: AUTHOR}],
            sources: [{archive: 'vektor-06c.ru', url: 'https://vektor-06c.ru/software/elite/'}],
        }));
        renderView(ID);

        await expect(screen.findByRole('list', {name: 'Авторы'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('region', {name: 'Ссылки'})).toBeInTheDocument();
    });

    it('shows developers and publishers in separate fields with links to their pages', async () => {
        stubFetch(json({...ELITE, credits: GROUP_CREDITS}));
        renderView(ID);

        const developers = await screen.findByRole('region', {name: 'Developer'});
        const publishers = screen.getByRole('region', {name: 'Publisher'});

        expect(within(developers).getAllByRole('link').map((link) => [link.textContent, link.getAttribute('href')])).toStrictEqual([
            ['Solar Studio', `/groups/${GROUP}`],
            ['Sol — Solar Studio', `/labels/${GROUP_LABEL}`],
        ]);
        expect(within(publishers).getAllByRole('link').map((link) => [link.textContent, link.getAttribute('href')])).toStrictEqual([
            ['J.P. — Jane Publisher', `/labels/${AUTHOR_LABEL}`],
            ['Sol — Solar Studio', `/labels/${GROUP_LABEL}`],
        ]);
        expect(screen.queryByRole('list', {name: 'Credits'})).not.toBeInTheDocument();
    });

    it('keeps the other authors and their roles in the general credits', async () => {
        stubFetch(json({...ELITE, credits: [
            ...GROUP_CREDITS,
            {id: AUTHOR, type: 'author', title: 'Jane Doe', roles: ['code'], ownerId: AUTHOR},
        ]}));
        renderView(ID);

        const credits = await screen.findByRole('list', {name: 'Credits'});

        expect(within(credits).getAllByRole('listitem').map((item) => item.textContent)).toStrictEqual(['Jane DoeCode']);
        expect(within(credits).getByRole('link', {name: 'Jane Doe'})).toHaveAttribute('href', `/authors/${AUTHOR}`);
    });

    it('omits the publisher field when nobody is credited as publisher and keeps authors with unknown roles', async () => {
        stubFetch(json({...ELITE, credits: [
            {id: AUTHOR, type: 'author', title: 'Jane Doe', roles: ['unknown'], ownerId: AUTHOR},
            {id: GROUP, type: 'group', title: 'Solar Studio', roles: ['developer'], ownerId: GROUP},
        ]}));
        renderView(ID);

        const credits = await screen.findByRole('list', {name: 'Credits'});

        expect(screen.getByRole('region', {name: 'Developer'})).toHaveTextContent('Solar Studio');
        expect(screen.queryByRole('region', {name: 'Publisher'})).not.toBeInTheDocument();
        expect(within(credits).getAllByRole('listitem').map((item) => item.textContent)).toStrictEqual(['Jane Doe']);
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

    it('shows the homepage and the imported page together, with each address shown once', async () => {
        const importedPage = 'https://vektor-06c.ru/programmnoe-obespechenie/arkadnye-igry/elite/';
        const homepage = 'https://example.org/elite/';
        stubFetch(json({
            ...ELITE,
            links: [{type: 'homepage', url: homepage}, {type: 'general', url: importedPage}],
            sources: [{archive: 'vektor-06c.ru', url: importedPage}],
        }));
        renderView(ID);

        const links = await screen.findByRole('region', {name: 'Links'});

        expect(within(links).getByRole('link', {name: 'vektor-06c.ru'})).toHaveAttribute('href', importedPage);
        expect(within(links).getByRole('link', {name: 'example.org'})).toHaveAttribute('href', homepage);
        expect(within(links).getAllByRole('link')).toHaveLength(2);
    });

    it('shows a homepage for a program without an import', async () => {
        const url = 'https://example.org/elite/';
        stubFetch(json({...ELITE, links: [{type: 'homepage', url}]}));
        renderView(ID);

        const links = await screen.findByRole('region', {name: 'Links'});

        expect(within(links).getByRole('link', {name: 'example.org'})).toHaveAttribute('href', url);
    });

    it('omits the links block for a program without links or sources', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        await screen.findByRole('heading', {level: 1, name: 'Elite'});

        expect(screen.queryByRole('region', {name: 'Links'})).not.toBeInTheDocument();
    });

    it('tells the preservation status of the software once it is known', async () => {
        stubFetch(json({...ELITE, preservation: 'missing-in-action'}));
        renderView(ID);

        await screen.findByRole('heading', {level: 1, name: 'Elite'});

        expect(detailLabels('Preservation status')).toHaveLength(1);
        expect(screen.getByText('Missing in action')).toBeInTheDocument();
    });

    it('tells the preservation status in Russian', async () => {
        await i18n.changeLanguage('ru');
        try {
            stubFetch(json({...ELITE, preservation: 'in-sales'}));
            renderView(ID);

            await screen.findByRole('heading', {level: 1, name: 'Elite'});

            expect(detailLabels('Статус сохранности')).toHaveLength(1);
            expect(screen.getByText('В продаже')).toBeInTheDocument();
        } finally {
            await i18n.changeLanguage('en');
        }
    });

    it('shows no preservation status while it is unknown', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        await screen.findByRole('heading', {level: 1, name: 'Elite'});

        expect(detailLabels('Preservation status')).toHaveLength(0);
    });

    it('tells where the software comes from: the kind, its reliability, the rework and the original', async () => {
        stubFetch(json({
            ...ELITE,
            lineage: {
                kind: 'port-with-changes',
                reliability: 'reliable',
                rework: ['original-material-used', 'graphics-and-sound-changed'],
                source: 'Elite (Acornsoft, 1984), BBC Micro',
            },
        }));
        renderView(ID);

        await screen.findByRole('heading', {level: 1, name: 'Elite'});

        expect(detailLabels('Lineage')).toHaveLength(1);
        expect(screen.getByText('Port with changes (reliable); material of the original used, graphics and sound changed')).toBeInTheDocument();
        expect(detailLabels('Original')).toHaveLength(1);
        expect(screen.getByText('Elite (Acornsoft, 1984), BBC Micro')).toBeInTheDocument();
    });

    it('leads by every web address in the original to its page', async () => {
        stubFetch(json({
            ...ELITE,
            lineage: {
                kind: 'port',
                reliability: null,
                rework: [],
                source: 'Elite, https://www.mobygames.com/game/elite/ and http://elite.example.org',
            },
        }));
        renderView(ID);

        await screen.findByRole('heading', {level: 1, name: 'Elite'});

        expect(screen.getByText('Port')).toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'https://www.mobygames.com/game/elite/'})).toHaveAttribute('href', 'https://www.mobygames.com/game/elite/');
        expect(screen.getByRole('link', {name: 'http://elite.example.org'})).toHaveAttribute('href', 'http://elite.example.org');
    });

    it('shows the original alone when the kind is unknown', async () => {
        stubFetch(json({...ELITE, lineage: {kind: null, reliability: null, rework: [], source: 'Elite, BBC Micro'}}));
        renderView(ID);

        await screen.findByRole('heading', {level: 1, name: 'Elite'});

        expect(detailLabels('Lineage')).toHaveLength(0);
        expect(detailLabels('Original')).toHaveLength(1);
        expect(screen.getByText('Elite, BBC Micro')).toBeInTheDocument();
    });

    it('tells the lineage in Russian', async () => {
        await i18n.changeLanguage('ru');
        try {
            stubFetch(json({
                ...ELITE,
                lineage: {kind: 'remake', reliability: 'assumption', rework: ['new-implementation'], source: 'Elite, BBC Micro'},
            }));
            renderView(ID);

            await screen.findByRole('heading', {level: 1, name: 'Elite'});

            expect(detailLabels('Происхождение')).toHaveLength(1);
            expect(screen.getByText('Ремейк (предположение); полностью новая реализация')).toBeInTheDocument();
            expect(detailLabels('Первоисточник')).toHaveLength(1);
        } finally {
            await i18n.changeLanguage('en');
        }
    });

    it('shows no lineage while it is unknown', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        await screen.findByRole('heading', {level: 1, name: 'Elite'});

        expect(detailLabels('Lineage')).toHaveLength(0);
        expect(detailLabels('Original')).toHaveLength(0);
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
        expect(screen.queryByRole('button', {name: 'Actions'})).not.toBeInTheDocument();
    });

    it('offers no editing of software the reader may not change, nor of its releases they may not change', async () => {
        stubFetch(json({...ELITE, canChange: false}), json([{...AY_MOD, canChange: false}]));
        renderView(ID);

        const releases = await screen.findByRole('region', {name: 'Releases'});
        await within(releases).findByRole('row', {name: /Elite AY/});
        await within(releases).findByRole('link', {name: 'Add release'});

        expect(screen.queryByRole('button', {name: 'Actions'})).not.toBeInTheDocument();
        expect(within(releases).queryByRole('link', {name: 'Edit Elite AY'})).not.toBeInTheDocument();
    });

    it('offers editing of a release the reader may change inside software they may not', async () => {
        stubFetch(json({...ELITE, canChange: false}));
        renderView(ID);

        const releases = await screen.findByRole('region', {name: 'Releases'});
        const release = await within(releases).findByRole('row', {name: /Elite AY/});

        expect(within(release).getByRole('link', {name: 'Edit Elite AY'})).toBeInTheDocument();
        expect(screen.queryByRole('button', {name: 'Actions'})).not.toBeInTheDocument();
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

    it('shows the rating of the software among its details', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        const rating = await screen.findByRole('region', {name: 'Rating'});

        await expect(within(rating).findByRole('group', {name: 'Rating 4.08 of 5'})).resolves.toHaveAttribute('title', '3 votes');
    });

    it('shows who voted on the software below its releases', async () => {
        stubFetch(json(ELITE), json([AY_MOD]), json(MEMBER), json([]), {votes: json([{voter: 'Zork', value: 5, castAt: '2026-11-24T10:00:00Z'}])});
        renderView(ID);

        const votes = await screen.findByRole('region', {name: 'Votes'});

        await expect(within(votes).findByText('Zork')).resolves.toBeInTheDocument();
        expect(screen.getByRole('region', {name: 'Releases'}).compareDocumentPosition(votes) & Node.DOCUMENT_POSITION_FOLLOWING).toBe(Node.DOCUMENT_POSITION_FOLLOWING);
    });

    it('shows the vote of the reader among the votes once they gave it', async () => {
        let voted = false;
        stubFetch(json(ELITE), json([AY_MOD]), json(MEMBER), json([]), {
            votes: () => voted ? json([{voter: 'Zed', value: 5, castAt: '2026-11-26T10:00:00Z'}])() : json([])(),
            vote: () => {
                voted = true;
                return json({rating: 3.9, votesAmount: 1, myVote: 5})();
            },
        });
        renderView(ID);

        const votes = await screen.findByRole('region', {name: 'Votes'});
        await within(votes).findByText('Nobody has voted yet.');

        await userEvent.click(screen.getByRole('button', {name: 'Give 5 stars'}));

        await expect(within(votes).findByText('Zed')).resolves.toBeInTheDocument();
    });
});
