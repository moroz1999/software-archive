import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {toAuthorId} from '@/entities/authors';
import {AuthorView} from './AuthorView';

const ID = '00000000-0000-0000-0003-000000000001';
const ZED = {
    id: ID,
    name: 'Zed',
    realName: 'John Doe',
    notes: 'Coder since 1984\nand musician',
    aliases: [{id: '00000000-0000-0000-0004-000000000001', name: 'Axe'}, {id: '00000000-0000-0000-0004-000000000002', name: 'Zed'}],
    locations: [{country: 'Norway', city: null, primary: false}, {country: 'Finland', city: 'Helsinki', primary: true}],
    groups: [{id: '00000000-0000-0000-0005-000000000001', name: 'Crew'}],
};

function renderView() {
    renderWithQueries(<MemoryRouter><AuthorView id={toAuthorId(ID)}/></MemoryRouter>);
}

function itemsOf(name: string): string[] {
    return within(screen.getByRole('list', {name})).getAllByRole('listitem').map((item) => item.textContent);
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

    it('shows everything known about the author under the main name', async () => {
        stubFetch(json(ZED));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Zed'});

        expect(screen.getByText('John Doe')).toBeInTheDocument();
        expect(itemsOf('Aliases')).toStrictEqual(['Axe', 'Zed']);
        expect(itemsOf('Groups')).toStrictEqual(['Crew']);
        expect(screen.getByText('Coder since 1984')).toBeInTheDocument();
        expect(screen.getByText('and musician')).toBeInTheDocument();
    });

    it('lists the primary location first, a city with its country', async () => {
        stubFetch(json(ZED));
        renderView();

        await screen.findByRole('heading', {level: 1, name: 'Zed'});

        expect(itemsOf('Locations')).toStrictEqual(['Helsinki, Finland', 'Norway']);
    });

    it('leaves out what is not known', async () => {
        stubFetch(json({...ZED, realName: null, notes: null, aliases: [], locations: [], groups: []}));
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
