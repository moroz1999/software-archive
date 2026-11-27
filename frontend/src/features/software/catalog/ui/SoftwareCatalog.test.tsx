import {Button} from '@/shared/ui/button';
import {SoftwareCardLayout, type SoftwareCardComponent, type SoftwareCardContentProps} from '@/entities/software';
import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, waitFor, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {PlatformSelectionProvider, PlatformSwitch} from '@/entities/platforms';
import {type components, subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, pageResponse, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {SOFTWARE_CATEGORY_ROUTE} from '../config/paths';
import {SoftwareCatalogProvider} from '../model/SoftwareCatalogProvider';
import {SoftwareCatalog} from './SoftwareCatalog';

const ELITE = {id: '00000000-0000-0000-00fd-000000000001', title: 'Elite', releaseDate: '1987-05-01', screenshots: [], madeBy: [], rating: 0, votesAmount: 0} satisfies components['schemas']['SoftwareListItemDto'];
const EXOLON = {id: '00000000-0000-0000-00fd-000000000002', title: 'Exolon', releaseDate: '1987-09-01', screenshots: [], madeBy: [], rating: 0, votesAmount: 0} satisfies components['schemas']['SoftwareListItemDto'];

const GAMES = '00000000-0000-0000-00fd-0000000000c1';
const ACTION = '00000000-0000-0000-00fd-0000000000c2';
const SHOOTERS = '00000000-0000-0000-00fd-0000000000c3';
const PUZZLE = '00000000-0000-0000-00fd-0000000000c4';
const DEMOSCENE = '00000000-0000-0000-00fd-0000000000c5';
const FINLAND = '00000000-0000-0000-00fd-0000000000f1';
const BRITAIN = '00000000-0000-0000-00fd-0000000000f2';
const HELSINKI = '00000000-0000-0000-00fd-0000000000e1';
const ESPOO = '00000000-0000-0000-00fd-0000000000e2';
const STOCKHOLM = '00000000-0000-0000-00fd-0000000000e3';

const TREE = [
    {id: GAMES, title: 'Games', children: [
        {id: ACTION, title: 'Action', children: [{id: SHOOTERS, title: 'Shooters', children: []}]},
        {id: PUZZLE, title: 'Puzzle', children: []},
    ]},
    {id: DEMOSCENE, title: 'Demoscene', children: []},
];
const COUNTRIES = [{id: BRITAIN, name: 'Britain'}, {id: FINLAND, name: 'Finland'}];

const FACETS = {
    years: [{value: 1987, count: 2}, {value: 1989, count: 1}],
    countries: [{value: FINLAND, count: 2}, {value: BRITAIN, count: 1}],
    // Stockholm comes with software also made in Finland; Espoo has none left.
    cities: [{value: HELSINKI, count: 2}, {value: STOCKHOLM, count: 1}],
    platforms: [{value: 'bk', count: 1}, {value: 'msx', count: 3}],
    hardware: [{value: 'vector06c', name: 'Vector-06C', group: 'computers', groupName: 'Computers', count: 2}, {value: 'ay8910', name: 'AY-3-8910', group: 'sound', groupName: 'Sound', count: 1}],
};
const FINNISH_CITIES = [{id: ESPOO, name: 'Espoo'}, {id: HELSINKI, name: 'Helsinki'}];


const ALL = '/api/software?page=0';

function catalog(items: readonly unknown[], {facets = FACETS, totalPages = 1, number = 0} = {}) {
    return json({...pageResponse(items, {number, totalPages, totalElements: items.length}), facets});
}

function routes(catalogs: Record<string, () => Response>): Map<string, () => Response> {
    return new Map([
        ['/api/platforms', json([{code: 'bk', name: 'BK'}, {code: 'msx', name: 'MSX'}])],
        ['/api/categories', json(TREE)],
        ['/api/countries', json(COUNTRIES)],
        [`/api/countries/${FINLAND}/cities`, json(FINNISH_CITIES)],
        ...Object.entries(catalogs),
    ]);
}

function CardLayout(props: SoftwareCardContentProps) {
    return <SoftwareCardLayout {...props} footer={null}/>;
}

function CardWithVotingButton(props: SoftwareCardContentProps) {
    return <SoftwareCardLayout {...props} footer={<Button>{`Vote on ${props.software.title}`}</Button>}/>;
}

function openCatalog(path = '/software', Card: SoftwareCardComponent = CardLayout) {
    const element = <PlatformSelectionProvider><PlatformSwitch/><SoftwareCatalogProvider><SoftwareCatalog Card={Card}/></SoftwareCatalogProvider></PlatformSelectionProvider>;
    const router = createMemoryRouter([
        {path: '/software', element},
        {path: SOFTWARE_CATEGORY_ROUTE, element},
    ], {initialEntries: [path]});
    renderWithQueries(<RouterProvider router={router}/>);
    return router;
}

async function cardTitles(): Promise<string[]> {
    await screen.findAllByRole('article');
    return screen.getAllByRole('article').map((card) => within(card).getByRole('heading').textContent);
}


describe(SoftwareCatalog, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        localStorage.clear();
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('shows a skeleton until the software arrives, then its cards', async () => {
        stubFetchRoutes(routes({[ALL]: catalog([ELITE, EXOLON])}));

        openCatalog();

        expect(screen.getByTestId('software-catalog-skeleton')).toBeInTheDocument();
        await expect(cardTitles()).resolves.toStrictEqual(['Elite', 'Exolon']);
        expect(screen.queryByTestId('software-catalog-skeleton')).not.toBeInTheDocument();
    });

    it('asks for the software of the chosen platform', async () => {
        localStorage.setItem('archive.platform', 'bk');
        const fetchMock = stubFetchRoutes(routes({[`${ALL}&platform=bk`]: catalog([ELITE])}));

        openCatalog();

        await expect(cardTitles()).resolves.toStrictEqual(['Elite']);
        expect(fetchMock).toHaveBeenCalledWith(`${ALL}&platform=bk`, expect.anything());
    });

    it('asks with the keyword and the filters of the address', async () => {
        const asked = `/api/software?page=1&q=elite&year=1987&category=${GAMES}&country=${FINLAND}`;
        stubFetchRoutes(routes({[asked]: catalog([ELITE], {number: 1, totalPages: 2})}));

        openCatalog(`/software/category/${GAMES}?q=elite&year=1987&country=${FINLAND}&page=2`);

        await expect(cardTitles()).resolves.toStrictEqual(['Elite']);
        expect(screen.getByRole('searchbox', {name: 'Search'})).toHaveValue('elite');
        expect(screen.getByRole('combobox', {name: 'Year'})).toHaveValue('1987');
        expect(screen.getByRole('combobox', {name: 'Country'})).toHaveValue(FINLAND);
    });

    it('offers the years with their counts and goes to the first page of the chosen one', async () => {
        stubFetchRoutes(routes({
            '/api/software?page=1': catalog([ELITE, EXOLON], {number: 1, totalPages: 2}),
            [`${ALL}&year=1987`]: catalog([ELITE, EXOLON]),
        }));
        const router = openCatalog('/software?page=2');

        const year = await screen.findByRole('combobox', {name: 'Year'});
        await screen.findByRole('option', {name: '1987 (2)'});

        expect(within(year).getAllByRole('option').map((option) => option.textContent)).toStrictEqual(['Any year', '1987 (2)', '1989 (1)']);

        await userEvent.selectOptions(year, '1987');

        expect(router.state.location.search).toBe('?year=1987');
    });

    it('offers the countries by name with their counts', async () => {
        stubFetchRoutes(routes({[ALL]: catalog([ELITE]), [`${ALL}&country=${BRITAIN}`]: catalog([EXOLON])}));
        const router = openCatalog();

        const country = await screen.findByRole('combobox', {name: 'Country'});
        await screen.findByRole('option', {name: 'Finland (2)'});

        expect(within(country).getAllByRole('option').map((option) => option.textContent)).toStrictEqual(['Any country', 'Finland (2)', 'Britain (1)']);

        await userEvent.selectOptions(country, BRITAIN);

        expect(router.state.location.search).toBe(`?country=${BRITAIN}`);
    });

    it('locks the city until a country is chosen', async () => {
        stubFetchRoutes(routes({[ALL]: catalog([ELITE])}));
        openCatalog();

        const city = await screen.findByRole('combobox', {name: 'City'});

        expect(city).toBeDisabled();
        expect(within(city).getAllByRole('option').map((option) => option.textContent)).toStrictEqual(['Any city']);
    });

    it('offers the cities of the chosen country with their counts and goes to the first page of the chosen one', async () => {
        stubFetchRoutes(routes({
            [`/api/software?page=1&country=${FINLAND}`]: catalog([ELITE], {number: 1, totalPages: 2}),
            [`${ALL}&country=${FINLAND}&city=${HELSINKI}`]: catalog([ELITE]),
        }));
        const router = openCatalog(`/software?country=${FINLAND}&page=2`);

        const city = await screen.findByRole('combobox', {name: 'City'});
        await within(city).findByRole('option', {name: 'Helsinki (2)'});

        expect(city).toBeEnabled();
        expect(within(city).getAllByRole('option').map((option) => option.textContent)).toStrictEqual(['Any city', 'Helsinki (2)']);

        await userEvent.selectOptions(city, HELSINKI);

        expect(router.state.location.search).toBe(`?country=${FINLAND}&city=${HELSINKI}`);
    });

    it('forgets the city once another country is chosen', async () => {
        stubFetchRoutes(routes({
            [`${ALL}&country=${FINLAND}&city=${HELSINKI}`]: catalog([ELITE]),
            [`${ALL}&country=${BRITAIN}`]: catalog([EXOLON]),
        }));
        const router = openCatalog(`/software?country=${FINLAND}&city=${HELSINKI}`);

        await screen.findByRole('option', {name: 'Britain (1)'});
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Country'}), BRITAIN);

        expect(router.state.location.search).toBe(`?country=${BRITAIN}`);
    });

    async function openHardware() {
        await userEvent.click(await screen.findByRole('button', {name: 'Hardware'}));
        return screen.findByRole('dialog', {name: 'Hardware'});
    }

    function checkboxNames(within_: HTMLElement): string[] {
        return within(within_).getAllByRole<HTMLInputElement>('checkbox').map((box) => box.labels?.[0]?.textContent ?? '');
    }

    it('offers the platforms and the hardware of every platform with their counts while all platforms are shown', async () => {
        stubFetchRoutes(routes({[ALL]: catalog([ELITE])}));
        openCatalog();

        const dialog = await openHardware();

        await expect(within(dialog).findByRole('group', {name: 'Platforms'})).resolves.toBeInTheDocument();
        expect(checkboxNames(within(dialog).getByRole('group', {name: 'Platforms'}))).toStrictEqual(['BK (1)', 'MSX (3)']);
        expect(checkboxNames(within(dialog).getByRole('group', {name: 'Computers'}))).toStrictEqual(['Vector-06C (2)']);
        expect(checkboxNames(within(dialog).getByRole('group', {name: 'Sound'}))).toStrictEqual(['AY-3-8910 (1)']);
    });

    it('offers the hardware of a group the archive added under the name of the group, after the others', async () => {
        const withVideo = {...FACETS, hardware: [...FACETS.hardware, {value: 'gpu', name: 'GPU', group: 'video', groupName: 'Video', count: 1}]};
        stubFetchRoutes(routes({[ALL]: catalog([ELITE], {facets: withVideo})}));
        openCatalog();

        const dialog = await openHardware();

        await expect(within(dialog).findByRole('group', {name: 'Video'})).resolves.toBeInTheDocument();
        expect(checkboxNames(within(dialog).getByRole('group', {name: 'Video'}))).toStrictEqual(['GPU (1)']);
        expect(within(dialog).getAllByRole('group').map((group) => group.getAttribute('aria-label'))).toStrictEqual(['Platforms', 'Computers', 'Sound', 'Video']);
    });

    it('offers only the hardware of the platform chosen in the switch, without the platforms', async () => {
        localStorage.setItem('archive.platform', 'bk');
        // The index offers only the hardware of the software of the platform.
        const ofBk = {...FACETS, hardware: FACETS.hardware.slice(1)};
        stubFetchRoutes(routes({[`${ALL}&platform=bk`]: catalog([ELITE], {facets: ofBk})}));
        openCatalog();

        const dialog = await openHardware();

        await expect(within(dialog).findByRole('group', {name: 'Sound'})).resolves.toBeInTheDocument();
        expect(within(dialog).queryByRole('group', {name: 'Platforms'})).not.toBeInTheDocument();
        expect(within(dialog).queryByRole('group', {name: 'Computers'})).not.toBeInTheDocument();
    });

    it('applies the platforms and the hardware checked from the first page, and names them on the button', async () => {
        stubFetchRoutes(routes({
            '/api/software?page=1': catalog([ELITE], {number: 1, totalPages: 2}),
            [`${ALL}&platforms=bk&hardware=ay8910`]: catalog([ELITE]),
        }));
        const router = openCatalog('/software?page=2');

        const dialog = await openHardware();
        await userEvent.click(await within(dialog).findByRole('checkbox', {name: 'BK (1)'}));
        await userEvent.click(within(dialog).getByRole('checkbox', {name: 'AY-3-8910 (1)'}));

        expect(router.state.location.search).toBe('?page=2');

        await userEvent.click(within(dialog).getByRole('button', {name: 'Apply'}));

        expect(router.state.location.search).toBe('?platforms=bk&hardware=ay8910');
        expect(screen.queryByRole('dialog', {name: 'Hardware'})).not.toBeInTheDocument();
        await expect(screen.findByRole('button', {name: 'Hardware: BK, AY-3-8910'})).resolves.toBeInTheDocument();
    });

    it('keeps the hardware checked in the address shown checked, and resets it all', async () => {
        stubFetchRoutes(routes({[`${ALL}&hardware=ay8910`]: catalog([ELITE]), [ALL]: catalog([ELITE])}));
        const router = openCatalog('/software?hardware=ay8910');

        await userEvent.click(await screen.findByRole('button', {name: 'Hardware: AY-3-8910'}));
        const dialog = await screen.findByRole('dialog', {name: 'Hardware'});

        await expect(within(dialog).findByRole('checkbox', {name: 'AY-3-8910 (1)'})).resolves.toBeChecked();

        await userEvent.click(within(dialog).getByRole('button', {name: 'Reset'}));

        expect(router.state.location.search).toBe('');
    });

    it('changes nothing when the hardware window is closed without applying', async () => {
        stubFetchRoutes(routes({[ALL]: catalog([ELITE])}));
        const router = openCatalog();

        const dialog = await openHardware();
        await userEvent.click(await within(dialog).findByRole('checkbox', {name: 'AY-3-8910 (1)'}));
        await userEvent.keyboard('{Escape}');

        expect(router.state.location.search).toBe('');
        expect(screen.getByRole('button', {name: 'Hardware'})).toBeInTheDocument();
    });

    it('offers the whole category tree to go through, folded, without counts', async () => {
        stubFetchRoutes(routes({[ALL]: catalog([ELITE])}));

        openCatalog();

        const categories = within(await screen.findByRole('navigation', {name: 'Categories'}));
        await categories.findByRole('link', {name: 'Games'});

        expect(categories.getByRole('link', {name: 'All categories'})).toHaveAttribute('aria-current', 'page');
        expect(categories.getByRole('link', {name: 'Games'})).toHaveAttribute('href', `/software/category/${GAMES}`);
        expect(categories.getByRole('link', {name: 'Demoscene'})).toBeInTheDocument();
        expect(categories.queryByRole('link', {name: 'Action'})).not.toBeInTheDocument();
    });

    it('unfolds a category to its subcategories', async () => {
        stubFetchRoutes(routes({[ALL]: catalog([ELITE])}));
        openCatalog();
        const categories = within(await screen.findByRole('navigation', {name: 'Categories'}));

        await userEvent.click(await categories.findByRole('button', {name: 'Expand Games'}));

        expect(categories.getByRole('link', {name: 'Action'})).toHaveAttribute('href', `/software/category/${ACTION}`);
        expect(categories.getByRole('link', {name: 'Puzzle'})).toBeInTheDocument();
    });

    it('opens with the path to the current category unfolded and the category marked', async () => {
        stubFetchRoutes(routes({[`/api/software?page=2&q=e&category=${SHOOTERS}`]: catalog([ELITE], {number: 2, totalPages: 3})}));

        openCatalog(`/software/category/${SHOOTERS}?q=e&page=3`);

        const categories = within(await screen.findByRole('navigation', {name: 'Categories'}));
        const shooters = await categories.findByRole('link', {name: 'Shooters'});

        expect(shooters).toHaveAttribute('aria-current', 'page');
        expect(shooters).toHaveAttribute('href', `/software/category/${SHOOTERS}?q=e`);
        expect(categories.getByRole('link', {name: 'All categories'})).toHaveAttribute('href', '/software?q=e');
        expect(categories.getByRole('link', {name: 'Games'})).not.toHaveAttribute('aria-current');
        expect(categories.getByRole('link', {name: 'Demoscene'})).toBeInTheDocument();
    });

    it('keeps the category in the path when another year is chosen', async () => {
        stubFetchRoutes(routes({
            [`${ALL}&category=${GAMES}`]: catalog([ELITE, EXOLON]),
            [`${ALL}&year=1989&category=${GAMES}`]: catalog([ELITE]),
        }));
        const router = openCatalog(`/software/category/${GAMES}`);

        await screen.findByRole('option', {name: '1989 (1)'});
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Year'}), '1989');

        expect(router.state.location.pathname).toBe(`/software/category/${GAMES}`);
        expect(router.state.location.search).toBe('?year=1989');
    });

    it('goes to the first page of the keyword once the visitor stops typing', async () => {
        stubFetchRoutes(routes({'/api/software?page=1': catalog([ELITE], {number: 1, totalPages: 2}), [`${ALL}&q=exo`]: catalog([EXOLON])}));
        const router = openCatalog('/software?page=2');

        await userEvent.type(await screen.findByRole('searchbox', {name: 'Search'}), 'exo');

        await waitFor(() => {
            expect(router.state.location.search).toBe('?q=exo');
        });
        await screen.findByRole('article', {name: 'Exolon'});

        await expect(cardTitles()).resolves.toStrictEqual(['Exolon']);
    });

    it('leads to the other pages of the same filter', async () => {
        stubFetchRoutes(routes({[`${ALL}&year=1987`]: catalog([ELITE], {totalPages: 3})}));

        openCatalog('/software?year=1987');

        const pages = await screen.findByRole('navigation', {name: 'Pages'});

        expect(within(pages).getByRole('link', {name: '2'})).toHaveAttribute('href', '/software?year=1987&page=2');
    });

    it('says when nothing is found', async () => {
        stubFetchRoutes(routes({[`${ALL}&q=zzz`]: catalog([], {facets: {years: [], countries: [], cities: [], platforms: [], hardware: []}})}));

        openCatalog('/software?q=zzz');

        await expect(screen.findByText('Nothing found.')).resolves.toBeInTheDocument();
    });

    it('locks the filters but not the search field and keeps the cards under a spinner while another filter loads', async () => {
        let respond: (response: Response) => void = () => undefined;
        const fetchMock = stubFetchRoutes(routes({[ALL]: catalog([ELITE, EXOLON])}));
        openCatalog();
        const year = await screen.findByRole('combobox', {name: 'Year'});
        await screen.findByRole('option', {name: '1987 (2)'});
        const base = fetchMock.getMockImplementation();
        fetchMock.mockImplementation((input, init) => {
            if (input === `${ALL}&year=1989`) {
                return new Promise((resolve) => {
                    respond = resolve;
                });
            }
            return base === undefined ? Promise.reject(new Error('no route')) : base(input, init);
        });

        await userEvent.selectOptions(year, '1989');

        await waitFor(() => {
            expect(screen.getByRole('combobox', {name: 'Year'})).toBeDisabled();
        });

        expect(screen.getByRole('article', {name: 'Elite'})).toBeInTheDocument();
        expect(screen.getByRole('searchbox', {name: 'Search'})).toBeEnabled();

        respond(catalog([ELITE])());

        await waitFor(() => {
            expect(screen.getByRole('combobox', {name: 'Year'})).toBeEnabled();
        });
    });

    it('shows the cards of a filter that answers at once with no spinner and no locked filters', async () => {
        stubFetchRoutes(routes({[ALL]: catalog([ELITE, EXOLON]), [`${ALL}&year=1989`]: catalog([EXOLON])}));
        openCatalog();
        await screen.findByRole('option', {name: '1989 (1)'});

        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Year'}), '1989');

        expect(screen.getByRole('combobox', {name: 'Year'})).toBeEnabled();

        await screen.findByRole('article', {name: 'Exolon'});
        await waitFor(() => {
            expect(screen.queryByRole('article', {name: 'Elite'})).not.toBeInTheDocument();
        });

        expect(screen.queryByTestId('loading-state-spinner')).not.toBeInTheDocument();
    });

    it('reports a system error when the catalog cannot be read', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => reports.push(report)));
        stubFetchRoutes(routes({[ALL]: json({detail: 'Internal server error.'}, 500)}));

        openCatalog();

        await waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(screen.queryAllByRole('article')).toHaveLength(0);
    });

    it('puts what it is given under each card, outside its link, with the software of the card', async () => {
        stubFetchRoutes(routes({[ALL]: catalog([{...ELITE, rating: 4.0769, votesAmount: 3}, EXOLON])}));
        openCatalog('/software', CardWithVotingButton);

        const [elite] = await screen.findAllByRole('article');
        const vote = within(elite ?? document.body).getByRole('button', {name: 'Vote on Elite'});

        expect(within(elite ?? document.body).getByRole('link', {name: 'Elite'})).not.toContainElement(vote);
        expect(screen.getByRole('button', {name: 'Vote on Exolon'})).toBeInTheDocument();
    });

    it('offers the cards by title or by rating and goes to the first page in the order chosen', async () => {
        stubFetchRoutes(routes({
            '/api/software?page=1&year=1987': catalog([ELITE, EXOLON], {number: 1, totalPages: 2}),
            '/api/software?page=0&year=1987&order=rating': catalog([EXOLON, ELITE]),
        }));
        const router = openCatalog('/software?year=1987&page=2');

        const order = await screen.findByRole('combobox', {name: 'Order'});

        expect(within(order).getAllByRole('option').map((option) => option.textContent)).toStrictEqual(['By title', 'By rating']);
        expect(order).toHaveValue('title');

        await userEvent.selectOptions(order, 'rating');

        expect(router.state.location.search).toBe('?year=1987&order=rating');

        await waitFor(() => {
            expect(screen.getAllByRole('article').map((card) => within(card).getByRole('heading').textContent)).toStrictEqual(['Exolon', 'Elite']);
        });
    });
});
