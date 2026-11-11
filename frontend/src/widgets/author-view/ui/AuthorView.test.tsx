import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
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
};

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
        stubFetch(json(ZED));
        renderView();

        expect(screen.getByTestId('author-view-skeleton')).toBeInTheDocument();
        await expect(screen.findByRole('heading', {level: 1, name: 'Zed'})).resolves.toBeInTheDocument();
        expect(screen.queryByTestId('author-view-skeleton')).not.toBeInTheDocument();
    });

    it('leads to the editing of the author', async () => {
        stubFetch(json(ZED));
        renderView();

        await expect(screen.findByRole('link', {name: 'Edit'})).resolves.toHaveAttribute('href', `/authors/${ID}/edit`);
    });

    it('shows everything known about the author under the main name', async () => {
        stubFetch(json(ZED));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Zed'});

        expect(screen.getByText('John Doe')).toBeInTheDocument();
        expect(itemsOf('Labels')).toStrictEqual(['Axe', 'Zed']);
        expect(screen.getByText('Coder since 1984')).toBeInTheDocument();
        expect(screen.getByText('and musician')).toBeInTheDocument();
    });

    it('leads to every group of the author, naming the label they were a member under unless it is the main name', async () => {
        stubFetch(json(ZED));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Zed'});
        const groups = screen.getByRole('list', {name: 'Groups'});

        expect(itemsOf('Groups')).toStrictEqual(['Crew as Axe', 'Band']);
        expect(within(groups).getByRole('link', {name: 'Crew'})).toHaveAttribute('href', `/groups/${CREW}`);
        expect(within(groups).getByRole('link', {name: 'Band'})).toHaveAttribute('href', `/groups/${BAND}`);
    });

    it('lists the primary location first, a city with its country', async () => {
        stubFetch(json(ZED));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Zed'});

        expect(itemsOf('Locations')).toStrictEqual(['Helsinki, Finland', 'Norway']);
    });

    it('leaves out what is not known', async () => {
        stubFetch(json({...ZED, realName: null, notes: null, labels: [], locations: [], groups: []}));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Zed'});

        expect(screen.queryByText('Real name')).not.toBeInTheDocument();
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
        expect(screen.queryByTestId('author-view-skeleton')).not.toBeInTheDocument();
        expect(screen.queryByRole('heading', {level: 1})).not.toBeInTheDocument();
    });
});
