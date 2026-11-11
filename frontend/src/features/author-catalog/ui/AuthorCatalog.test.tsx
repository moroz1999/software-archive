import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {PlatformSelectionProvider, PlatformSwitch} from '@/entities/platforms';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, pageResponse, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {AuthorCatalogProvider} from '../model/AuthorCatalogProvider';
import {AuthorCatalog} from './AuthorCatalog';

const ZED = '00000000-0000-0000-0003-000000000001';
const BOB = '00000000-0000-0000-0003-000000000002';
const FINLAND = '00000000-0000-0000-0002-000000000001';
const NORWAY = '00000000-0000-0000-0002-000000000002';
const HELSINKI = '00000000-0000-0000-0002-000000000101';

const CARL = '00000000-0000-0000-0003-000000000003';

const ENTRIES = [
    {personId: ZED, name: 'Axe', realName: 'John Doe', labelOf: 'Zed'},
    {personId: BOB, name: 'Bob', realName: null, labelOf: null},
    {personId: ZED, name: 'Zed', realName: 'John Doe', labelOf: null},
];
/** A person without a real name, with a label; and a person known only by the real name. */
const WITHOUT_REAL_NAMES = [
    {personId: BOB, name: 'Mona', realName: null, labelOf: 'Bob'},
    {personId: CARL, name: 'Carl Real', realName: null, labelOf: null},
];
const COUNTRIES = [{id: FINLAND, name: 'Finland'}, {id: NORWAY, name: 'Norway'}];
const FINNISH_CITIES = [{id: HELSINKI, name: 'Helsinki'}];

const ALL = '/api/authors?page=0&size=100';

function catalogPage(items: readonly unknown[], totalPages = 1, number = 0) {
    return json(pageResponse(items, {size: 100, number, totalPages, totalElements: items.length}));
}

function routes(entries: Record<string, () => Response>): Map<string, () => Response> {
    return new Map([['/api/platforms', json([{code: 'bk', name: 'BK'}, {code: 'msx', name: 'MSX'}])], ['/api/countries', json(COUNTRIES)], [`/api/countries/${FINLAND}/cities`, json(FINNISH_CITIES)], ...Object.entries(entries)]);
}

function openCatalog(path = '/authors') {
    const router = createMemoryRouter([
        {path: '/authors', element: <PlatformSelectionProvider><PlatformSwitch/><AuthorCatalogProvider><AuthorCatalog/></AuthorCatalogProvider></PlatformSelectionProvider>},
    ], {initialEntries: [path]});
    renderWithQueries(<RouterProvider router={router}/>);
    return router;
}

/** What every entry says: its name and what is shown next to it. */
async function entryLines(): Promise<string[]> {
    const list = await screen.findByRole('list', {name: 'Authors'});
    return within(list).getAllByRole('listitem').map((item) => item.textContent.replaceAll(/\s+/gu, ' ').trim());
}

async function entryNames(): Promise<string[]> {
    const list = await screen.findByRole('list', {name: 'Authors'});
    return within(list).getAllByRole('link').map((link) => link.textContent);
}

describe(AuthorCatalog, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        localStorage.clear();
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('follows the platform switch', async () => {
        localStorage.setItem('archive.platform', 'bk');
        const fetchMock = stubFetchRoutes(routes({[`${ALL}&platform=bk`]: catalogPage(ENTRIES), [`${ALL}&platform=msx`]: catalogPage([])}));
        openCatalog();

        await screen.findByRole('list', {name: 'Authors'});
        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Platform'}), await screen.findByRole('option', {name: 'MSX'}));

        await vi.waitFor(() => {
            expect(fetchMock.mock.calls.map(([url]) => url)).toContain(`${ALL}&platform=msx`);
        });
    });

    it('shows a skeleton until the entries arrive', async () => {
        stubFetchRoutes(routes({[ALL]: catalogPage(ENTRIES)}));
        openCatalog();

        expect(screen.getByTestId('author-catalog-skeleton')).toBeInTheDocument();
        await expect(entryNames()).resolves.toStrictEqual(['Axe', 'Bob', 'Zed']);
        expect(screen.queryByTestId('author-catalog-skeleton')).not.toBeInTheDocument();
    });

    it('leads from every entry to its person', async () => {
        stubFetchRoutes(routes({[ALL]: catalogPage(ENTRIES)}));
        openCatalog();

        const list = await screen.findByRole('list', {name: 'Authors'});

        expect(within(list).getByRole('link', {name: 'Axe'})).toHaveAttribute('href', `/authors/${ZED}`);
        expect(within(list).getByRole('link', {name: 'Bob'})).toHaveAttribute('href', `/authors/${BOB}`);
    });

    it('names the real name of the person next to the nickname and next to every label', async () => {
        stubFetchRoutes(routes({[ALL]: catalogPage(ENTRIES)}));
        openCatalog();

        await expect(entryLines()).resolves.toStrictEqual(['Axe John Doe', 'Bob', 'Zed John Doe']);
    });

    it('names the main name next to a label of a person without a real name, and nothing next to a real name', async () => {
        stubFetchRoutes(routes({[ALL]: catalogPage(WITHOUT_REAL_NAMES)}));
        openCatalog();

        await expect(entryLines()).resolves.toStrictEqual(['Mona label of Bob', 'Carl Real']);
    });

    it('offers all letters, the current one marked, each keeping the place filters and starting from the first page', async () => {
        const filtered = `/authors?letter=B&country=${FINLAND}&page=2`;
        stubFetchRoutes(routes({[`/api/authors?page=1&size=100&letter=B&country=${FINLAND}`]: catalogPage(ENTRIES, 2, 1)}));
        openCatalog(filtered);

        const letters = await screen.findByRole('navigation', {name: 'First letter'});

        expect(within(letters).getAllByRole('listitem')).toHaveLength(1 + 26 + 32 + 1);
        expect(within(letters).getByRole('link', {name: 'B'})).toHaveAttribute('aria-current', 'page');
        expect(within(letters).getByRole('link', {name: 'Я'})).toHaveAttribute('href', `/authors?letter=%D0%AF&country=${FINLAND}`);
        expect(within(letters).getByRole('link', {name: '#'})).toHaveAttribute('href', `/authors?letter=%23&country=${FINLAND}`);
        expect(within(letters).getByRole('link', {name: 'All'})).toHaveAttribute('href', `/authors?country=${FINLAND}`);
    });

    it('offers no city before a country is chosen', async () => {
        stubFetchRoutes(routes({[ALL]: catalogPage(ENTRIES)}));
        openCatalog();
        await entryNames();

        expect(screen.getByRole('combobox', {name: 'City'})).toBeDisabled();
    });

    it('narrows the catalog to the chosen country, then offers its cities', async () => {
        stubFetchRoutes(routes({
            [ALL]: catalogPage(ENTRIES),
            [`/api/authors?page=0&size=100&country=${FINLAND}`]: catalogPage([ENTRIES[2]]),
            [`/api/authors?page=0&size=100&country=${FINLAND}&city=${HELSINKI}`]: catalogPage([ENTRIES[0]]),
        }));
        const router = openCatalog();
        await entryNames();

        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Country'}), 'Finland');

        await vi.waitFor(async () => {
            await expect(entryNames()).resolves.toStrictEqual(['Zed']);
        });

        expect(router.state.location.search).toBe(`?country=${FINLAND}`);

        const city = screen.getByRole('combobox', {name: 'City'});
        await vi.waitFor(() => {
            expect(city).toBeEnabled();
        });
        await userEvent.selectOptions(city, 'Helsinki');

        await vi.waitFor(async () => {
            await expect(entryNames()).resolves.toStrictEqual(['Axe']);
        });

        expect(router.state.location.search).toBe(`?country=${FINLAND}&city=${HELSINKI}`);
    });

    it('forgets the city when the country changes', async () => {
        stubFetchRoutes(routes({
            [`/api/authors?page=0&size=100&country=${FINLAND}&city=${HELSINKI}`]: catalogPage(ENTRIES),
            [`/api/authors?page=0&size=100&country=${NORWAY}`]: catalogPage([]),
            [`/api/countries/${NORWAY}/cities`]: json([]),
        }));
        const router = openCatalog(`/authors?country=${FINLAND}&city=${HELSINKI}`);
        await entryNames();

        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Country'}), 'Norway');

        expect(router.state.location.search).toBe(`?country=${NORWAY}`);
    });

    it('keeps the shown entries under a spinner, the controls locked, while another filter loads', async () => {
        let answerFinland: (() => void) | undefined;
        const fetchMock = stubFetchRoutes(routes({[ALL]: catalogPage(ENTRIES)}));
        const original = fetchMock.getMockImplementation();
        fetchMock.mockImplementation(async (input, init) => {
            if (input === `/api/authors?page=0&size=100&country=${FINLAND}`) {
                await new Promise<void>((resolve) => {
                    answerFinland = resolve;
                });
                return catalogPage([ENTRIES[2]])();
            }
            return original === undefined ? Promise.reject(new Error('no stub')) : original(input, init);
        });
        openCatalog();
        await entryNames();

        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Country'}), 'Finland');

        await vi.waitFor(() => {
            expect(screen.getByTestId('loading-state-spinner')).toHaveAttribute('aria-hidden', 'false');
        });

        await expect(entryNames()).resolves.toStrictEqual(['Axe', 'Bob', 'Zed']);
        expect(within(screen.getByRole('navigation', {name: 'First letter'})).queryByRole('link')).not.toBeInTheDocument();

        answerFinland?.();

        await vi.waitFor(async () => {
            await expect(entryNames()).resolves.toStrictEqual(['Zed']);
        });

        expect(screen.getByTestId('loading-state-spinner')).toHaveAttribute('aria-hidden', 'true');
    });

    it('shows the entries of a filter that answers at once with no spinner and no locked controls', async () => {
        stubFetchRoutes(routes({[ALL]: catalogPage(ENTRIES), [`/api/authors?page=0&size=100&country=${FINLAND}`]: catalogPage([ENTRIES[2]])}));
        openCatalog();
        await entryNames();

        await userEvent.selectOptions(screen.getByRole('combobox', {name: 'Country'}), 'Finland');

        expect(screen.getByRole('combobox', {name: 'Country'})).toBeEnabled();

        await vi.waitFor(async () => {
            await expect(entryNames()).resolves.toStrictEqual(['Zed']);
        });

        expect(screen.queryByTestId('loading-state-spinner')).not.toBeInTheDocument();
    });

    it('leads to the other pages of the catalog', async () => {
        stubFetchRoutes(routes({[`/api/authors?page=0&size=100&letter=A`]: catalogPage(ENTRIES, 3)}));
        openCatalog('/authors?letter=A');

        const pages = await screen.findByRole('navigation', {name: 'Pages'});

        expect(within(pages).getByRole('link', {name: '2'})).toHaveAttribute('href', '/authors?letter=A&page=2');
        expect(within(pages).getByText('1')).toHaveAttribute('aria-current', 'page');
    });

    it('says so when no author matches', async () => {
        stubFetchRoutes(routes({[`/api/authors?page=0&size=100&letter=Q`]: catalogPage([])}));
        openCatalog('/authors?letter=Q');

        await expect(screen.findByText('No authors found.')).resolves.toBeInTheDocument();
        expect(screen.queryByRole('navigation', {name: 'Pages'})).not.toBeInTheDocument();
    });

    it('never sends a filter that cannot be one', async () => {
        const fetchMock = stubFetchRoutes(routes({[ALL]: catalogPage(ENTRIES)}));
        openCatalog('/authors?letter=AB&country=42&page=x');

        await entryNames();

        expect(fetchMock.mock.calls.map(([url]) => url)).toContain(ALL);
    });

    it('hands a system error of the entries to the error dialog and shows nothing of its own', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetchRoutes(routes({[ALL]: json({status: 500}, 500)}));
        openCatalog();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(reports[0]?.error.kind).toBe('server');
        expect(screen.queryByTestId('author-catalog-skeleton')).not.toBeInTheDocument();
        expect(screen.queryByRole('list', {name: 'Authors'})).not.toBeInTheDocument();
    });

    it('hands a system error of the countries to the error dialog and keeps the catalog', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetchRoutes(new Map([['/api/platforms', json([])], [ALL, catalogPage(ENTRIES)], ['/api/countries', json({status: 500}, 500)]]));
        openCatalog();

        await expect(entryNames()).resolves.toStrictEqual(['Axe', 'Bob', 'Zed']);

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(screen.getByRole('combobox', {name: 'Country'})).toBeDisabled();
    });
});
