import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, waitFor, within} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {toAuthorId} from '@/entities/authors';
import {AuthorView} from './AuthorView';

const ID = '00000000-0000-0000-0003-000000000001';
const CREW = '00000000-0000-0000-0005-000000000001';
const BAND = '00000000-0000-0000-0005-000000000002';
const ZED = {
    id: ID,
    name: 'Zed',
    realName: 'John Doe',
    notes: 'Coder since 1984\nand musician',
    labels: [{id: '00000000-0000-0000-0004-000000000001', name: 'Axe', display: false}, {id: '00000000-0000-0000-0004-000000000002', name: 'Zed', display: true}],
    locations: [
        {countryId: '00000000-0000-0000-0002-000000000002', country: 'Norway', cityId: null, city: null, primary: false},
        {countryId: '00000000-0000-0000-0002-000000000001', country: 'Finland', cityId: '00000000-0000-0000-0002-000000000011', city: 'Helsinki', primary: true},
    ],
    groups: [{id: CREW, name: 'Crew', as: 'Axe'}, {id: BAND, name: 'Band', as: null}],
    added: {by: 'Zork', at: '2026-03-04T05:06:07Z'},
    canChange: true,
};

const SOFTWARE_URL = `/api/authors/${ID}/software`;
const NO_SOFTWARE = new Map([[SOFTWARE_URL, json([])]]);
const ELITE = '00000000-0000-0000-0001-000000000001';
const TETRIS = '00000000-0000-0000-0001-000000000002';

function renderView() {
    renderWithQueries(<MemoryRouter><AuthorView id={toAuthorId(ID)}/></MemoryRouter>);
}

function itemsOf(name: string): string[] {
    return within(screen.getByRole('list', {name})).getAllByRole('listitem').map((item) => item.textContent.replaceAll(/\s+/gu, ' ').trim());
}

describe(AuthorView, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('shows a skeleton until the author arrives', async () => {
        stubFetchAround(NO_SOFTWARE, json(ZED));
        renderView();

        expect(screen.getByTestId('author-view-skeleton')).toBeInTheDocument();
        await expect(screen.findByRole('heading', {level: 1, name: 'Zed'})).resolves.toBeInTheDocument();
        expect(screen.queryByTestId('author-view-skeleton')).not.toBeInTheDocument();
    });

    it('lists the software the author is credited on as cards with their roles and the other labels they used', async () => {
        stubFetchAround(new Map([[SOFTWARE_URL, json([
            {id: ELITE, title: 'Elite', releaseDate: '1985', screenshots: [], madeBy: ['Zed'], credits: [
                {as: null, roles: ['code']},
                {as: 'Axe', roles: ['music', 'unknown']},
            ]},
            {id: TETRIS, title: 'Tetris', releaseDate: null, screenshots: [], madeBy: [], credits: [{as: null, roles: ['unknown']}]},
        ])]]), json(ZED));
        renderView();

        const software = await screen.findByRole('region', {name: 'Software'});
        const [elite, tetris] = await within(software).findAllByRole('article');

        expect(within(software).getByRole('link', {name: /Elite/u})).toHaveAttribute('href', `/software/${ELITE}`);
        expect(elite).toHaveTextContent('Code');
        expect(elite).toHaveTextContent('as AxeMusic');
        expect(tetris).not.toHaveTextContent('Unknown');
    });

    it('leaves the software out for an author credited on none', async () => {
        stubFetchAround(NO_SOFTWARE, json(ZED));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Zed'});

        await waitFor(() => {
            expect(screen.queryByRole('region', {name: 'Software'})).not.toBeInTheDocument();
        });
    });

    it('leads to the editing of the author', async () => {
        stubFetchAround(NO_SOFTWARE, json(ZED));
        renderView();

        await expect(screen.findByRole('link', {name: 'Edit'})).resolves.toHaveAttribute('href', `/authors/${ID}/edit`);
    });

    it('shows everything known about the author under the main name', async () => {
        stubFetchAround(NO_SOFTWARE, json(ZED));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Zed'});

        expect(screen.getByText('John Doe')).toBeInTheDocument();
        expect(itemsOf('Labels')).toStrictEqual(['Axe', 'Zed']);
        expect(screen.getByText('Coder since 1984')).toBeInTheDocument();
        expect(screen.getByText('and musician')).toBeInTheDocument();
    });

    it('leads to the page of every label of the author', async () => {
        stubFetchAround(NO_SOFTWARE, json(ZED));
        renderView();

        const labels = await screen.findByRole('list', {name: 'Labels'});

        expect(within(labels).getByRole('link', {name: 'Axe'})).toHaveAttribute('href', '/labels/00000000-0000-0000-0004-000000000001');
        expect(within(labels).getByRole('link', {name: 'Zed'})).toHaveAttribute('href', '/labels/00000000-0000-0000-0004-000000000002');
    });

    it('leads to every group of the author, naming the label they were a member under unless it is the main name', async () => {
        stubFetchAround(NO_SOFTWARE, json(ZED));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Zed'});
        const groups = screen.getByRole('list', {name: 'Groups'});

        expect(itemsOf('Groups')).toStrictEqual(['Crew as Axe', 'Band']);
        expect(within(groups).getByRole('link', {name: 'Crew'})).toHaveAttribute('href', `/groups/${CREW}`);
        expect(within(groups).getByRole('link', {name: 'Band'})).toHaveAttribute('href', `/groups/${BAND}`);
    });

    it('lists the primary location first, a city with its country', async () => {
        stubFetchAround(NO_SOFTWARE, json(ZED));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Zed'});

        expect(itemsOf('Locations')).toStrictEqual(['Helsinki, Finland', 'Norway']);
    });

    it('leaves out what is not known', async () => {
        stubFetchAround(NO_SOFTWARE, json({...ZED, realName: null, notes: null, labels: [], locations: [], groups: []}));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Zed'});
        await waitFor(() => {
            expect(screen.queryByRole('region', {name: 'Software'})).not.toBeInTheDocument();
        });

        expect(screen.queryByText('Real name')).not.toBeInTheDocument();
        expect(screen.queryByRole('list')).not.toBeInTheDocument();
    });

    it('hands a system error to the error dialog and shows nothing of its own', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetchAround(NO_SOFTWARE, json({status: 500}, 500));
        renderView();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(reports[0]?.error.kind).toBe('server');
        expect(screen.queryByTestId('author-view-skeleton')).not.toBeInTheDocument();
        expect(screen.queryByRole('heading', {level: 1})).not.toBeInTheDocument();
    });

    it('offers no editing of the author to whoever may not change it', async () => {
        stubFetchAround(NO_SOFTWARE, json({...ZED, canChange: false}));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Zed'});

        expect(screen.queryByRole('link', {name: 'Edit'})).not.toBeInTheDocument();
    });

    it('tells who added the author and when', async () => {
        stubFetchAround(NO_SOFTWARE, json(ZED));
        renderView();

        await expect(screen.findByText('Added by Zork on 4 March 2026')).resolves.toBeInTheDocument();
    });

    it('tells only when the author was added once the account of whoever added them is gone', async () => {
        stubFetchAround(NO_SOFTWARE, json({...ZED, added: {by: null, at: '2026-03-04T05:06:07Z'}}));
        renderView();

        await expect(screen.findByText('Added on 4 March 2026')).resolves.toBeInTheDocument();
    });
});
