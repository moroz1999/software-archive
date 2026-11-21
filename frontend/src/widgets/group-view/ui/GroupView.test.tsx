import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, waitFor, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {breadcrumbSteps, json, renderWithQueries, stubFetchAround} from '@/shared/lib/test';
import {toGroupId} from '@/entities/groups';
import {GroupView} from './GroupView';

const ID = '00000000-0000-0000-0005-000000000001';
const GHOST = '00000000-0000-0000-0004-000000000099';
const BAND = '00000000-0000-0000-0005-000000000002';
const OLDIES = '00000000-0000-0000-0005-000000000003';
const FEDYA = '00000000-0000-0000-0003-000000000001';
const ZED = '00000000-0000-0000-0003-000000000002';
const CARL = '00000000-0000-0000-0003-000000000003';
const FEDYA_LABEL = '00000000-0000-0000-0004-000000000011';
const ZED_LABEL = '00000000-0000-0000-0004-000000000012';
/** Crew Ltd, known also as Crew and CRW. */
const CREW = {
    id: ID,
    name: 'Crew Ltd',
    notes: 'Since 1990\nand still going',
    types: ['demogroup', 'label'],
    labels: [{id: '00000000-0000-0000-0004-000000000001', name: 'Crew'}, {id: '00000000-0000-0000-0004-000000000002', name: 'CRW'}],
    locations: [
        {countryId: '00000000-0000-0000-0002-000000000002', country: 'Norway', cityId: null, city: null, primary: false},
        {countryId: '00000000-0000-0000-0002-000000000001', country: 'Finland', cityId: '00000000-0000-0000-0002-000000000011', city: 'Helsinki', primary: true},
    ],
    members: [
        {id: GHOST, type: 'author-label', title: 'Ghost', name: 'Ghost', personId: null, personName: null, roles: [], startDate: null, endDate: null},
        {id: CARL, type: 'author', title: 'Carl Real', name: 'Carl Real', personId: CARL, personName: null, roles: [], startDate: null, endDate: null},
        {id: ZED_LABEL, type: 'author-label', title: 'Zed', name: 'Zed', personId: ZED, personName: null, roles: [], startDate: null, endDate: null},
        {id: FEDYA_LABEL, type: 'author-label', title: 'хакер федя', name: 'хакер федя', personId: FEDYA, personName: 'MASTERSOFT', roles: [], startDate: null, endDate: null},
    ],
    memberGroups: [{id: BAND, name: 'Band'}],
    groups: [{id: OLDIES, name: 'Alpha'}],
    added: {by: 'Zork', at: '2026-03-04T05:06:07Z'},
    canChange: true,
};

const SOFTWARE_URL = `/api/groups/${ID}/software`;
const NO_SOFTWARE = new Map([[SOFTWARE_URL, json([])]]);
const ELITE = '00000000-0000-0000-0001-000000000001';
const TETRIS = '00000000-0000-0000-0001-000000000002';

function renderView() {
    renderWithQueries(<MemoryRouter><GroupView id={toGroupId(ID)}/></MemoryRouter>);
}

function itemsOf(name: string): string[] {
    return within(screen.getByRole('list', {name})).getAllByRole('listitem').map((item) => item.textContent.replaceAll(/\s+/gu, ' ').trim());
}

describe(GroupView, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('shows a skeleton until the group arrives', async () => {
        stubFetchAround(NO_SOFTWARE, json(CREW));
        renderView();

        expect(screen.getByTestId('group-view-skeleton')).toBeInTheDocument();
        await expect(screen.findByRole('heading', {level: 1, name: 'Crew Ltd'})).resolves.toBeInTheDocument();
        expect(screen.queryByTestId('group-view-skeleton')).not.toBeInTheDocument();
    });

    it('leads back from the group to the groups', async () => {
        stubFetchAround(NO_SOFTWARE, json(CREW));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Crew Ltd'});

        expect(breadcrumbSteps()).toStrictEqual([['Groups', '/groups'], ['Crew Ltd', null]]);
    });

    it('lists the software the group is credited on as cards with its roles', async () => {
        stubFetchAround(new Map([[SOFTWARE_URL, json([
            {id: ELITE, title: 'Elite', releaseDate: '1985', screenshots: [], madeBy: ['Crew'], credits: [{as: null, roles: ['developer']}]},
            {id: TETRIS, title: 'Tetris', releaseDate: null, screenshots: [], madeBy: [], credits: [{as: 'CRW', roles: ['publisher']}]},
        ])]]), json(CREW));
        renderView();

        const software = await screen.findByRole('region', {name: 'Software'});
        const [elite, tetris] = await within(software).findAllByRole('article');

        expect(elite).toHaveTextContent('Developer');
        expect(tetris).toHaveTextContent('as CRWPublisher');
    });

    it('leads to the editing of the group', async () => {
        stubFetchAround(NO_SOFTWARE, json(CREW));
        renderView();

        const menu = await screen.findByRole('button', {name: 'Actions'});

        expect(screen.queryByRole('link', {name: 'Edit'})).not.toBeInTheDocument();

        await userEvent.click(menu);

        expect(screen.getByRole('link', {name: 'Edit'})).toHaveAttribute('href', `/groups/${ID}/edit`);
    });

    it('shows everything known about the group under the main name', async () => {
        stubFetchAround(NO_SOFTWARE, json(CREW));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Crew Ltd'});

        expect(screen.queryByText('Name')).not.toBeInTheDocument();
        expect(itemsOf('Types')).toStrictEqual(['demogroup', 'label']);
        expect(itemsOf('Labels')).toStrictEqual(['Crew', 'CRW']);
        expect(screen.getByText('Since 1990')).toBeInTheDocument();
        expect(screen.getByText('and still going')).toBeInTheDocument();
    });

    it('leads to the page of every label of the group', async () => {
        stubFetchAround(NO_SOFTWARE, json(CREW));
        renderView();

        const labels = await screen.findByRole('list', {name: 'Labels'});

        expect(within(labels).getByRole('link', {name: 'Crew'})).toHaveAttribute('href', '/labels/00000000-0000-0000-0004-000000000001');
        expect(within(labels).getByRole('link', {name: 'CRW'})).toHaveAttribute('href', '/labels/00000000-0000-0000-0004-000000000002');
    });

    it('lists the primary location first, a city with its country', async () => {
        stubFetchAround(NO_SOFTWARE, json(CREW));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Crew Ltd'});

        expect(itemsOf('Locations')).toStrictEqual(['Helsinki, Finland', 'Norway']);
    });

    it('lists the members under the labels they were members under, with the main name of the person when it differs', async () => {
        stubFetchAround(NO_SOFTWARE, json(CREW));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Crew Ltd'});

        expect(itemsOf('Members')).toStrictEqual(['Ghost', 'Carl Real', 'Zed', 'хакер федя MASTERSOFT']);
    });

    it('leads from every label of a member to its page, and from a person to the person', async () => {
        stubFetchAround(NO_SOFTWARE, json(CREW));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Crew Ltd'});
        const members = screen.getByRole('list', {name: 'Members'});

        expect(within(members).getByRole('link', {name: 'хакер федя'})).toHaveAttribute('href', `/labels/${FEDYA_LABEL}`);
        expect(within(members).getByRole('link', {name: 'MASTERSOFT'})).toHaveAttribute('href', `/authors/${FEDYA}`);
        expect(within(members).getByRole('link', {name: 'Zed'})).toHaveAttribute('href', `/labels/${ZED_LABEL}`);
        expect(within(members).getByRole('link', {name: 'Carl Real'})).toHaveAttribute('href', `/authors/${CARL}`);
        expect(within(members).getByRole('link', {name: 'Ghost'})).toHaveAttribute('href', `/labels/${GHOST}`);
    });

    it('shows the known roles of the members and leaves the unknown one out', async () => {
        stubFetchAround(NO_SOFTWARE, json({...CREW, members: [
            {id: ZED, type: 'author', title: 'Zed', name: 'Zed', personId: ZED, personName: null, roles: ['coder'], startDate: null, endDate: null},
            {id: FEDYA, type: 'author', title: 'хакер федя', name: 'хакер федя', personId: FEDYA, personName: null, roles: ['unknown'], startDate: null, endDate: null},
        ]}));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Crew Ltd'});

        expect(itemsOf('Members')).toStrictEqual(['ZedCoder', 'хакер федя']);
    });

    it('leads to the groups inside it and to the groups it is inside', async () => {
        stubFetchAround(NO_SOFTWARE, json(CREW));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Crew Ltd'});

        expect(within(screen.getByRole('list', {name: 'Member groups'})).getByRole('link', {name: 'Band'})).toHaveAttribute('href', `/groups/${BAND}`);
        expect(within(screen.getByRole('list', {name: 'Member of'})).getByRole('link', {name: 'Alpha'})).toHaveAttribute('href', `/groups/${OLDIES}`);
    });

    it('leaves out what is not known', async () => {
        stubFetchAround(NO_SOFTWARE, json({...CREW, notes: null, types: [], labels: [], locations: [], members: [], memberGroups: [], groups: []}));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Crew Ltd'});
        await waitFor(() => {
            expect(screen.queryByRole('region', {name: 'Software'})).not.toBeInTheDocument();
        });

        expect(screen.queryByText('Notes')).not.toBeInTheDocument();
        // The breadcrumbs are the only list left.
        expect(screen.getAllByRole('list')).toStrictEqual([within(screen.getByRole('navigation', {name: 'Breadcrumbs'})).getByRole('list')]);
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
        expect(screen.queryByTestId('group-view-skeleton')).not.toBeInTheDocument();
        expect(screen.queryByRole('heading', {level: 1})).not.toBeInTheDocument();
    });

    it('offers no editing of the group to whoever may not change it', async () => {
        stubFetchAround(NO_SOFTWARE, json({...CREW, canChange: false}));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Crew Ltd'});

        expect(screen.queryByRole('button', {name: 'Actions'})).not.toBeInTheDocument();
    });

    it('tells who added the group and when', async () => {
        stubFetchAround(NO_SOFTWARE, json(CREW));
        renderView();

        await expect(screen.findByText('Added by Zork on 4 March 2026')).resolves.toBeInTheDocument();
    });

    it('tells only when the group was added once the account of whoever added it is gone', async () => {
        stubFetchAround(NO_SOFTWARE, json({...CREW, added: {by: null, at: '2026-03-04T05:06:07Z'}}));
        renderView();

        await expect(screen.findByText('Added on 4 March 2026')).resolves.toBeInTheDocument();
    });
});
