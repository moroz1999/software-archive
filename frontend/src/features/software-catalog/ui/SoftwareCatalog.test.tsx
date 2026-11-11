import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, waitFor, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {PlatformSelectionProvider, PlatformSwitch} from '@/entities/platforms';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, pageResponse, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {SOFTWARE_CATEGORY_ROUTE} from '../config/paths';
import {SoftwareCatalogProvider} from '../model/SoftwareCatalogProvider';
import {SoftwareCatalog} from './SoftwareCatalog';

const ELITE = {id: '00000000-0000-0000-00fd-000000000001', title: 'Elite', releaseDate: '1987-05-01'};
const EXOLON = {id: '00000000-0000-0000-00fd-000000000002', title: 'Exolon', releaseDate: '1987-09-01'};

const GAMES = '00000000-0000-0000-00fd-0000000000c1';
const ACTION = '00000000-0000-0000-00fd-0000000000c2';
const SHOOTERS = '00000000-0000-0000-00fd-0000000000c3';
const PUZZLE = '00000000-0000-0000-00fd-0000000000c4';
const DEMOSCENE = '00000000-0000-0000-00fd-0000000000c5';
const FINLAND = '00000000-0000-0000-00fd-0000000000f1';
const BRITAIN = '00000000-0000-0000-00fd-0000000000f2';

const TREE = [
    {id: GAMES, title: 'Games', children: [
        {id: ACTION, title: 'Action', children: [{id: SHOOTERS, title: 'Shooters', children: []}]},
        {id: PUZZLE, title: 'Puzzle', children: []},
    ]},
    {id: DEMOSCENE, title: 'Demoscene', children: []},
];
const COUNTRIES = [{id: BRITAIN, name: 'Britain'}, {id: FINLAND, name: 'Finland'}];

/** Demoscene has no software here: the catalog does not offer it. */
const FACETS = {
    years: [{value: 1987, count: 2}, {value: 1989, count: 1}],
    categories: [{value: GAMES, count: 3}, {value: ACTION, count: 2}, {value: SHOOTERS, count: 1}, {value: PUZZLE, count: 1}],
    countries: [{value: FINLAND, count: 2}, {value: BRITAIN, count: 1}],
};

const ALL = '/api/software?page=0&size=20';

function catalog(items: readonly unknown[], {facets = FACETS, totalPages = 1, number = 0} = {}) {
    return json({...pageResponse(items, {number, totalPages, totalElements: items.length}), facets});
}

function routes(catalogs: Record<string, () => Response>): Map<string, () => Response> {
    return new Map([
        ['/api/platforms', json([{code: 'bk', name: 'BK'}, {code: 'msx', name: 'MSX'}])],
        ['/api/categories', json(TREE)],
        ['/api/countries', json(COUNTRIES)],
        ...Object.entries(catalogs),
    ]);
}

function openCatalog(path = '/software') {
    const element = <PlatformSelectionProvider><PlatformSwitch/><SoftwareCatalogProvider><SoftwareCatalog/></SoftwareCatalogProvider></PlatformSelectionProvider>;
    const router = createMemoryRouter([
        {path: '/software', element},
        {path: SOFTWARE_CATEGORY_ROUTE, element},
    ], {initialEntries: [path]});
    renderWithQueries(<RouterProvider router={router}/>);
    return router;
}

async function cardTitles(): Promise<string[]> {
    await screen.findAllByRole('article');
    return screen.getAllByRole('article').map((card) => within(card).getAllByRole('link')[0]?.textContent ?? '');
}

function chipsOf(name: string): string[] {
    return within(screen.getByRole('navigation', {name})).getAllByRole('listitem').map((item) => item.textContent.trim());
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
        const asked = `/api/software?page=1&size=20&q=elite&year=1987&category=${GAMES}&country=${FINLAND}`;
        stubFetchRoutes(routes({[asked]: catalog([ELITE], {number: 1, totalPages: 2})}));

        openCatalog(`/software/category/${GAMES}?q=elite&year=1987&country=${FINLAND}&page=2`);

        await expect(cardTitles()).resolves.toStrictEqual(['Elite']);
        expect(screen.getByRole('searchbox', {name: 'Search'})).toHaveValue('elite');
        expect(screen.getByRole('combobox', {name: 'Year'})).toHaveValue('1987');
        expect(screen.getByRole('combobox', {name: 'Country'})).toHaveValue(FINLAND);
    });

    it('offers the years with their counts and goes to the first page of the chosen one', async () => {
        stubFetchRoutes(routes({
            '/api/software?page=1&size=20': catalog([ELITE, EXOLON], {number: 1, totalPages: 2}),
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

    it('offers the top of the category tree that has software, with counts', async () => {
        stubFetchRoutes(routes({[ALL]: catalog([ELITE])}));

        openCatalog();

        await screen.findByRole('link', {name: 'Games (3)'});

        expect(chipsOf('Subcategories')).toStrictEqual(['Games (3)']);
        expect(screen.getByRole('link', {name: 'Games (3)'})).toHaveAttribute('href', `/software/category/${GAMES}`);
    });

    it('shows the path to the chosen category and offers its subcategories', async () => {
        stubFetchRoutes(routes({[`/api/software?page=2&size=20&q=e&category=${ACTION}`]: catalog([ELITE], {number: 2, totalPages: 3})}));

        openCatalog(`/software/category/${ACTION}?q=e&page=3`);

        await screen.findByRole('link', {name: 'Shooters (1)'});

        expect(chipsOf('Category')).toStrictEqual(['All categories', 'Games', 'Action']);
        expect(within(screen.getByRole('navigation', {name: 'Category'})).getByRole('link', {name: 'All categories'}))
            .toHaveAttribute('href', '/software?q=e');
        expect(within(screen.getByRole('navigation', {name: 'Category'})).getByRole('link', {name: 'Games'}))
            .toHaveAttribute('href', `/software/category/${GAMES}?q=e`);
        expect(chipsOf('Subcategories')).toStrictEqual(['Shooters (1)']);
        expect(screen.getByRole('link', {name: 'Shooters (1)'})).toHaveAttribute('href', `/software/category/${SHOOTERS}?q=e`);
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
        stubFetchRoutes(routes({'/api/software?page=1&size=20': catalog([ELITE], {number: 1, totalPages: 2}), [`${ALL}&q=exo`]: catalog([EXOLON])}));
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
        stubFetchRoutes(routes({[`${ALL}&q=zzz`]: catalog([], {facets: {years: [], categories: [], countries: []}})}));

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
});
