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

const FOUND = {
    software: {items: [{id: ELITE, title: 'Elite', releaseDate: '1987-05-01'}], total: 12},
    authors: {items: [{id: ZED, name: 'Zed', realName: 'John Doe'}, {id: CARL, name: 'Carl', realName: null}], total: 2},
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
    return router;
}

async function openWindow() {
    await userEvent.click(screen.getByRole('button', {name: 'Search'}));
    return screen.getByRole('dialog', {name: 'Search'});
}

async function search(keyword: string) {
    const window = await openWindow();
    await userEvent.type(within(window).getByRole('searchbox', {name: 'Search'}), keyword);
    return window;
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

    it('opens a window with the field ready for typing', async () => {
        stubFetchRoutes(routes({}));
        openPage();

        const window = await openWindow();

        expect(within(window).getByRole('searchbox', {name: 'Search'})).toHaveFocus();
    });

    it('shows the first matches of software and groups with their counts once the visitor stops typing', async () => {
        stubFetchRoutes(routes({'/api/search/quick?q=ze': json(FOUND)}));
        openPage();

        const window = await search('ze');

        await within(window).findByRole('link', {name: 'Elite'});

        expect(within(window).getByRole('heading', {name: 'Software (12)'})).toBeInTheDocument();
        expect(within(window).getByRole('link', {name: 'Elite'})).toHaveAttribute('href', `/software/${ELITE}`);
        expect(within(window).getByRole('heading', {name: 'Groups (1)'})).toBeInTheDocument();
        expect(within(window).getByRole('link', {name: 'ZS'})).toHaveAttribute('href', `/groups/${SQUAD}`);
    });

    it('shows people under the main name with the real name next to it when it is another one', async () => {
        stubFetchRoutes(routes({'/api/search/quick?q=ze': json(FOUND)}));
        openPage();

        const window = await search('ze');

        await within(window).findByRole('link', {name: 'Zed'});

        expect(within(window).getByRole('heading', {name: 'Authors (2)'})).toBeInTheDocument();
        expect(within(window).getByRole('link', {name: 'Zed'})).toHaveAttribute('href', `/authors/${ZED}`);
        expect(within(window).getByText('John Doe')).toBeInTheDocument();
        expect(within(window).getByRole('link', {name: 'Carl'})).toHaveAttribute('href', `/authors/${CARL}`);
    });

    it('leads from the count of software to the catalog of the keyword', async () => {
        stubFetchRoutes(routes({'/api/search/quick?q=ze': json(FOUND)}));
        openPage();

        const window = await search('ze');

        await expect(within(window).findByRole('link', {name: 'All software (12)'})).resolves.toHaveAttribute('href', '/software?q=ze');
    });

    it('asks only once the visitor stops typing, and for the chosen platform', async () => {
        localStorage.setItem('archive.platform', 'bk');
        const fetchMock = stubFetchRoutes(routes({'/api/search/quick?q=zed&platform=bk': json(FOUND)}));
        openPage();

        const window = await search('zed');

        await within(window).findByRole('link', {name: 'Elite'});

        expect(fetchMock.mock.calls.map(([url]) => url).filter((url) => typeof url === 'string' && url.startsWith('/api/search'))).toStrictEqual([
            '/api/search/quick?q=zed&platform=bk',
        ]);
    });

    it('asks nothing for a blank keyword', async () => {
        const fetchMock = stubFetchRoutes(routes({}));
        openPage();

        await search('   ');

        await new Promise((resolve) => {
            setTimeout(resolve, 400);
        });

        expect(fetchMock.mock.calls.map(([url]) => url).filter((url) => typeof url === 'string' && url.startsWith('/api/search'))).toStrictEqual([]);
    });

    it('leaves out the sections without matches', async () => {
        stubFetchRoutes(routes({'/api/search/quick?q=zs': json(ONLY_GROUPS)}));
        openPage();

        const window = await search('zs');

        await within(window).findByRole('link', {name: 'ZS'});

        expect(within(window).queryByRole('heading', {name: /Software/u})).not.toBeInTheDocument();
        expect(within(window).queryByRole('heading', {name: /Authors/u})).not.toBeInTheDocument();
    });

    it('says when nothing is found', async () => {
        stubFetchRoutes(routes({'/api/search/quick?q=zzz': json(NOTHING)}));
        openPage();

        const window = await search('zzz');

        await expect(within(window).findByText('Nothing found.')).resolves.toBeInTheDocument();
    });

    it('goes to a match and closes the window', async () => {
        stubFetchRoutes(routes({'/api/search/quick?q=ze': json(FOUND)}));
        const router = openPage();
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

        expect(within(window).getByRole('searchbox', {name: 'Search'})).toHaveValue('');
        expect(within(window).queryByRole('link')).not.toBeInTheDocument();
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
        await within(window).findByRole('link', {name: 'Elite'});

        await userEvent.type(within(window).getByRole('searchbox', {name: 'Search'}), 'd');

        await waitFor(() => {
            expect(fetchMock).toHaveBeenCalledWith('/api/search/quick?q=zed', expect.anything());
        });

        expect(within(window).getByRole('link', {name: 'Elite'})).toBeInTheDocument();

        respond(json(ONLY_GROUPS)());

        await waitFor(() => {
            expect(within(window).queryByRole('link', {name: 'Elite'})).not.toBeInTheDocument();
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

        expect(within(window).queryByRole('link')).not.toBeInTheDocument();
    });
});
