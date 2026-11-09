import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {toGroupId} from '@/entities/groups';
import {GroupView} from './GroupView';

const ID = '00000000-0000-0000-0005-000000000001';
const BAND = '00000000-0000-0000-0005-000000000002';
const OLDIES = '00000000-0000-0000-0005-000000000003';
const FEDYA = '00000000-0000-0000-0003-000000000001';
const ZED = '00000000-0000-0000-0003-000000000002';
const CREW = {
    id: ID,
    name: 'Crew',
    groupName: 'Crew Ltd',
    notes: 'Since 1990\nand still going',
    types: ['demogroup', 'label'],
    labels: [{id: '00000000-0000-0000-0004-000000000001', name: 'Crew'}, {id: '00000000-0000-0000-0004-000000000002', name: 'CRW'}],
    locations: [{country: 'Norway', city: null, primary: false}, {country: 'Finland', city: 'Helsinki', primary: true}],
    members: [
        {name: 'Ghost', personId: null, personName: null},
        {name: 'Zed', personId: ZED, personName: null},
        {name: 'хакер федя', personId: FEDYA, personName: 'MASTERSOFT'},
    ],
    memberGroups: [{id: BAND, name: 'Band'}],
    groups: [{id: OLDIES, name: 'Alpha'}],
};

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
        stubFetch(json(CREW));
        renderView();

        expect(screen.getByTestId('group-view-skeleton')).toBeInTheDocument();
        await expect(screen.findByRole('heading', {level: 1, name: 'Crew'})).resolves.toBeInTheDocument();
        expect(screen.queryByTestId('group-view-skeleton')).not.toBeInTheDocument();
    });

    it('shows everything known about the group under the main name', async () => {
        stubFetch(json(CREW));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Crew'});

        expect(screen.getByText('Crew Ltd')).toBeInTheDocument();
        expect(itemsOf('Types')).toStrictEqual(['demogroup', 'label']);
        expect(itemsOf('Labels')).toStrictEqual(['Crew', 'CRW']);
        expect(screen.getByText('Since 1990')).toBeInTheDocument();
        expect(screen.getByText('and still going')).toBeInTheDocument();
    });

    it('lists the primary location first, a city with its country', async () => {
        stubFetch(json(CREW));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Crew'});

        expect(itemsOf('Locations')).toStrictEqual(['Helsinki, Finland', 'Norway']);
    });

    it('lists the members under the labels they were members under, with the main name of the person when it differs', async () => {
        stubFetch(json(CREW));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Crew'});
        const members = screen.getByRole('list', {name: 'Members'});

        expect(itemsOf('Members')).toStrictEqual(['Ghost', 'Zed', 'хакер федя MASTERSOFT']);
        expect(within(members).getByRole('link', {name: 'хакер федя'})).toHaveAttribute('href', `/authors/${FEDYA}`);
        expect(within(members).getByRole('link', {name: 'Zed'})).toHaveAttribute('href', `/authors/${ZED}`);
        expect(within(members).queryByRole('link', {name: 'Ghost'})).not.toBeInTheDocument();
    });

    it('leads to the groups inside it and to the groups it is inside', async () => {
        stubFetch(json(CREW));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Crew'});

        expect(within(screen.getByRole('list', {name: 'Member groups'})).getByRole('link', {name: 'Band'})).toHaveAttribute('href', `/groups/${BAND}`);
        expect(within(screen.getByRole('list', {name: 'Member of'})).getByRole('link', {name: 'Alpha'})).toHaveAttribute('href', `/groups/${OLDIES}`);
    });

    it('leaves out what is not known', async () => {
        stubFetch(json({...CREW, notes: null, types: [], labels: [], locations: [], members: [], memberGroups: [], groups: []}));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Crew'});

        expect(screen.queryByText('Notes')).not.toBeInTheDocument();
        expect(screen.queryByRole('list')).not.toBeInTheDocument();
    });

    it('hands a system error to the error dialog and shows nothing of its own', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetch(json({status: 500}, 500));
        renderView();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(reports[0]?.error.kind).toBe('server');
        expect(screen.queryByTestId('group-view-skeleton')).not.toBeInTheDocument();
        expect(screen.queryByRole('heading', {level: 1})).not.toBeInTheDocument();
    });
});
