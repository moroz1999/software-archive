import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, waitFor, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {PlatformSelectionProvider, PlatformSwitch} from '@/entities/platforms';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {QuickSearch} from './QuickSearch';

const ELITE = '00000000-0000-0000-00fd-000000000001';
const ZED = '00000000-0000-0000-00fd-000000000011';
const CARL = '00000000-0000-0000-00fd-000000000012';
const SQUAD = '00000000-0000-0000-00fd-000000000021';
const PHANTOM = '00000000-0000-0000-00fd-000000000031';

const FOUND = {
    software: {items: [{id: ELITE, title: 'Elite', releaseDate: '1987-05-01'}], total: 12},
    authors: {items: [{id: ZED, name: 'Zed', realName: 'John Doe', kind: 'person'}, {id: CARL, name: 'Carl', realName: null, kind: 'person'}], total: 2},
    groups: {items: [{id: SQUAD, name: 'ZS'}], total: 1},
};
const NOTHING = {software: {items: [], total: 0}, authors: {items: [], total: 0}, groups: {items: [], total: 0}};
const ONLY_GROUPS = {...NOTHING, groups: FOUND.groups};

function routes(answers: Record<string, () => Response>): Map<string, () => Response> {
    return new Map([['/api/platforms', json([{code: 'bk', name: 'BK'}])], ...Object.entries(answers)]);
}

function openPage() {
    const router = createMemoryRouter([
        {path: '*', element: <PlatformSelectionProvider><PlatformSwitch/><QuickSearch/></PlatformSelectionProvider>},
    ], {initialEntries: ['/']});
    renderWithQueries(<RouterProvider router={router}/>);
    return {router};
}

async function openWindow() {
    await userEvent.click(screen.getByRole('button', {name: 'Search'}));
    return screen.getByRole('dialog', {name: 'Search'});
}

function fieldOf(window: HTMLElement) {
    return within(window).getByRole('combobox', {name: 'Search'});
}

async function search(keyword: string) {
    const window = await openWindow();
    await userEvent.type(fieldOf(window), keyword);
    return window;
}

function searched(fetchMock: {mock: {calls: unknown[][]}}): unknown[] {
    return fetchMock.mock.calls.map(([url]) => url).filter((url) => typeof url === 'string' && url.startsWith('/api/search'));
}

function location(router: ReturnType<typeof openPage>['router']): string {
    return `${router.state.location.pathname}${router.state.location.search}`;
}

describe(QuickSearch, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        localStorage.clear();
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('shows the search button as an icon named for screen readers', () => {
        stubFetchRoutes(routes({}));
        openPage();

        expect(screen.getByRole('button', {name: 'Search'})).toHaveTextContent(/^$/u);
    });

    it('opens a window of the field and its cross, the field ready for typing', async () => {
        stubFetchRoutes(routes({}));
        openPage();

        const window = await openWindow();

        expect(fieldOf(window)).toHaveFocus();
        expect(fieldOf(window)).toHaveAttribute('placeholder', 'Search');
        expect(within(window).queryByRole('heading')).not.toBeInTheDocument();
    });

    it('closes the window by its cross', async () => {
        stubFetchRoutes(routes({}));
        openPage();
        const window = await openWindow();

        await userEvent.click(within(window).getByRole('button', {name: 'Close'}));

        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('shows the first matches of people, groups and software, each kind with its count', async () => {
        stubFetchRoutes(routes({'/api/search/quick?q=ze': json(FOUND)}));
        openPage();

        const window = await search('ze');

        await within(window).findByRole('option', {name: 'Zed'});

        expect(within(window).getAllByRole('group').map((group) => within(group).getAllByText(/\(\d+\)$/u)[0]?.textContent)).toStrictEqual([
            'Authors (2)', 'Groups (1)', 'Software (12)',
        ]);
        expect(within(within(window).getByRole('group', {name: 'Authors (2)'})).getAllByRole('option').map((option) => option.textContent)).toStrictEqual(['Zed', 'Carl']);
        expect(within(window).getByRole('link', {name: 'Zed'})).toHaveAttribute('href', `/authors/${ZED}`);
        expect(within(window).getByRole('link', {name: 'ZS'})).toHaveAttribute('href', `/groups/${SQUAD}`);
        expect(within(window).getByRole('link', {name: 'Elite'})).toHaveAttribute('href', `/software/${ELITE}`);
    });

    it('shows a label of nobody among the people with nothing next to it, leading to the label\'s page', async () => {
        stubFetchRoutes(routes({'/api/search/quick?q=ph': json({
            ...NOTHING,
            authors: {items: [{id: PHANTOM, name: 'Phantom', realName: null, kind: 'label'}], total: 1},
        })}));
        openPage();

        const window = await search('ph');

        const option = await within(window).findByRole('option', {name: /Phantom/u});

        expect(option).toHaveTextContent(/^Phantom$/u);
        expect(within(option).getByRole('link', {name: /Phantom/u})).toHaveAttribute('href', `/labels/${PHANTOM}`);
    });

    it('names every kind by its caption', async () => {
        stubFetchRoutes(routes({'/api/search/quick?q=ze': json(FOUND)}));
        openPage();

        const window = await search('ze');

        await expect(within(window).findByRole('group', {name: 'Authors (2)'})).resolves.toBeInTheDocument();
    });

    it('asks once the visitor stops typing, for the chosen platform', async () => {
        localStorage.setItem('archive.platform', 'bk');
        const fetchMock = stubFetchRoutes(routes({'/api/search/quick?q=zed&platform=bk': json(FOUND)}));
        openPage();

        const window = await search('zed');

        await within(window).findByRole('option', {name: 'Elite'});

        expect(searched(fetchMock)).toStrictEqual(['/api/search/quick?q=zed&platform=bk']);
    });

    it('asks nothing for less than two letters', async () => {
        const fetchMock = stubFetchRoutes(routes({}));
        openPage();

        await search(' z ');

        await new Promise((resolve) => {
            setTimeout(resolve, 600);
        });

        expect(searched(fetchMock)).toStrictEqual([]);
    });

    it('leaves out the kinds without matches', async () => {
        stubFetchRoutes(routes({'/api/search/quick?q=zs': json(ONLY_GROUPS)}));
        openPage();

        const window = await search('zs');

        await within(window).findByRole('option', {name: 'ZS'});

        expect(within(window).getAllByRole('group')).toHaveLength(1);
    });

    it('says when nothing is found', async () => {
        stubFetchRoutes(routes({'/api/search/quick?q=zzz': json(NOTHING)}));
        openPage();

        const window = await search('zzz');

        await expect(within(window).findByText('Nothing found.')).resolves.toBeInTheDocument();
    });

    it('offers to find all only once something is typed', async () => {
        stubFetchRoutes(routes({'/api/search/quick?q=zed+squad': json(FOUND)}));
        openPage();
        const window = await openWindow();

        expect(within(window).queryByRole('link', {name: 'Find all'})).not.toBeInTheDocument();

        await userEvent.type(fieldOf(window), ' zed squad');

        expect(within(window).getByRole('link', {name: 'Find all'})).toHaveAttribute('href', '/search?q=zed+squad');
    });

    it('goes to the full search by the link and closes the window', async () => {
        stubFetchRoutes(routes({'/api/search/quick?q=zed': json(FOUND)}));
        const {router} = openPage();
        const window = await search('zed');

        await userEvent.click(within(window).getByRole('link', {name: 'Find all'}));

        expect(location(router)).toBe('/search?q=zed');
        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('goes to the full search by Enter with no match chosen, without waiting for the matches', async () => {
        stubFetchRoutes(routes({}));
        const {router} = openPage();
        const window = await openWindow();

        await userEvent.type(fieldOf(window), 'zed{Enter}');

        expect(location(router)).toBe('/search?q=zed');
        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('goes nowhere by Enter in a blank field', async () => {
        stubFetchRoutes(routes({}));
        const {router} = openPage();
        const window = await openWindow();

        await userEvent.type(fieldOf(window), '{Enter}');

        expect(location(router)).toBe('/');
        expect(screen.getByRole('dialog', {name: 'Search'})).toBeInTheDocument();
    });

    it('chooses the matches by the arrow keys round the list, the focus staying in the field', async () => {
        stubFetchRoutes(routes({'/api/search/quick?q=ze': json(FOUND)}));
        openPage();
        const window = await search('ze');
        const field = fieldOf(window);
        await within(window).findByRole('option', {name: 'Zed'});

        await userEvent.keyboard('{ArrowDown}');

        const zed = within(window).getByRole('option', {name: 'Zed'});

        expect(zed).toHaveAttribute('aria-selected', 'true');
        expect(field).toHaveAttribute('aria-activedescendant', zed.id);
        expect(field).toHaveFocus();

        await userEvent.keyboard('{ArrowDown}{ArrowDown}{ArrowDown}{ArrowDown}');

        expect(within(window).getByRole('option', {name: 'Zed'})).toHaveAttribute('aria-selected', 'true');

        await userEvent.keyboard('{ArrowUp}');

        expect(within(window).getByRole('option', {name: 'Elite'})).toHaveAttribute('aria-selected', 'true');
    });

    it('chooses the last match by the up arrow at first', async () => {
        stubFetchRoutes(routes({'/api/search/quick?q=ze': json(FOUND)}));
        openPage();
        const window = await search('ze');
        await within(window).findByRole('option', {name: 'Zed'});

        await userEvent.keyboard('{ArrowUp}');

        expect(within(window).getByRole('option', {name: 'Elite'})).toHaveAttribute('aria-selected', 'true');
    });

    it('opens the chosen match by Enter and closes the window', async () => {
        stubFetchRoutes(routes({'/api/search/quick?q=ze': json(FOUND)}));
        const {router} = openPage();
        const window = await search('ze');
        await within(window).findByRole('option', {name: 'Zed'});

        await userEvent.keyboard('{ArrowDown}{ArrowDown}{ArrowDown}{Enter}');

        expect(location(router)).toBe(`/groups/${SQUAD}`);
        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('forgets the chosen match once the visitor types on', async () => {
        stubFetchRoutes(routes({'/api/search/quick?q=ze': json(FOUND), '/api/search/quick?q=zed': json(FOUND)}));
        openPage();
        const window = await search('ze');
        await within(window).findByRole('option', {name: 'Zed'});

        await userEvent.keyboard('{ArrowDown}d');

        expect(within(window).queryByRole('option', {selected: true})).not.toBeInTheDocument();
        expect(fieldOf(window)).not.toHaveAttribute('aria-activedescendant');
    });

    it('goes to a match by a click and closes the window', async () => {
        stubFetchRoutes(routes({'/api/search/quick?q=ze': json(FOUND)}));
        const {router} = openPage();
        const window = await search('ze');

        await userEvent.click(await within(window).findByRole('link', {name: 'Zed'}));

        expect(router.state.location.pathname).toBe(`/authors/${ZED}`);
        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('closes on Escape and opens again with an empty field', async () => {
        stubFetchRoutes(routes({'/api/search/quick?q=ze': json(FOUND)}));
        openPage();
        await search('ze');

        await userEvent.keyboard('{Escape}');

        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();

        const window = await openWindow();

        expect(fieldOf(window)).toHaveValue('');
        expect(within(window).queryByRole('option')).not.toBeInTheDocument();
    });

    it('keeps the shown matches while the next keyword loads', async () => {
        let respond: (response: Response) => void = () => undefined;
        const fetchMock = stubFetchRoutes(routes({'/api/search/quick?q=ze': json(FOUND)}));
        const base = fetchMock.getMockImplementation();
        fetchMock.mockImplementation((input, init) => {
            if (input === '/api/search/quick?q=zed') {
                return new Promise((resolve) => {
                    respond = resolve;
                });
            }
            return base === undefined ? Promise.reject(new Error('no route')) : base(input, init);
        });
        openPage();
        const window = await search('ze');
        await within(window).findByRole('option', {name: 'Elite'});

        await userEvent.type(fieldOf(window), 'd');

        await waitFor(() => {
            expect(fetchMock).toHaveBeenCalledWith('/api/search/quick?q=zed', expect.anything());
        });

        expect(within(window).getByRole('option', {name: 'Elite'})).toBeInTheDocument();

        respond(json(ONLY_GROUPS)());

        await waitFor(() => {
            expect(within(window).queryByRole('option', {name: 'Elite'})).not.toBeInTheDocument();
        });
    });

    it('reports a system error and shows no matches', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => reports.push(report)));
        stubFetchRoutes(routes({'/api/search/quick?q=ze': json({detail: 'Internal server error.'}, 500)}));
        openPage();

        const window = await search('ze');

        await waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(within(window).queryByRole('option')).not.toBeInTheDocument();
    });
});
