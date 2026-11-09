import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, pageResponse, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {SoftwareListProvider} from '../model/SoftwareListProvider';
import {SoftwareList} from './SoftwareList';

const ALPHA = {id: '00000000-0000-0000-0001-000000000001', title: 'Fixture Software Alpha', releaseDate: '1985-03-02'};
const BETA = {id: '00000000-0000-0000-0001-000000000002', title: 'Fixture Software Beta', releaseDate: null};

describe(SoftwareList, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('shows a skeleton until the software arrives', async () => {
        stubFetch(json(pageResponse([ALPHA])));

        renderWithQueries(<MemoryRouter><SoftwareListProvider><SoftwareList/></SoftwareListProvider></MemoryRouter>);

        expect(screen.getByTestId('software-list-skeleton')).toBeInTheDocument();
        await expect(screen.findByText('Fixture Software Alpha')).resolves.toBeInTheDocument();
        expect(screen.queryByTestId('software-list-skeleton')).not.toBeInTheDocument();
    });

    it('lists the software as cards', async () => {
        stubFetch(json(pageResponse([ALPHA, BETA])));

        renderWithQueries(<MemoryRouter><SoftwareListProvider><SoftwareList/></SoftwareListProvider></MemoryRouter>);

        await expect(screen.findByRole('article', {name: 'Fixture Software Alpha'})).resolves.toHaveTextContent('1985');
        expect(screen.getByRole('article', {name: 'Fixture Software Beta'})).toBeInTheDocument();
        expect(screen.getAllByRole('listitem')).toHaveLength(2);
    });

    it('leads from a card to its software', async () => {
        stubFetch(json(pageResponse([ALPHA])));

        renderWithQueries(<MemoryRouter><SoftwareListProvider><SoftwareList/></SoftwareListProvider></MemoryRouter>);

        await expect(screen.findByRole('link', {name: 'Fixture Software Alpha'})).resolves.toHaveAttribute('href', `/software/${ALPHA.id}`);
    });

    it('says so when the archive has no software', async () => {
        stubFetch(json(pageResponse([])));

        renderWithQueries(<MemoryRouter><SoftwareListProvider><SoftwareList/></SoftwareListProvider></MemoryRouter>);

        await expect(screen.findByText('No software in the archive yet.')).resolves.toBeInTheDocument();
    });

    it('hands a business error to the error dialog, whose retry loads the list again', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetch(json({status: 409, code: 'software.changed'}, 409), json(pageResponse([ALPHA])));

        renderWithQueries(<MemoryRouter><SoftwareListProvider><SoftwareList/></SoftwareListProvider></MemoryRouter>);

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(screen.queryByRole('alert')).not.toBeInTheDocument();

        reports[0]?.retry?.();

        await expect(screen.findByText('Fixture Software Alpha')).resolves.toBeInTheDocument();
    });

    it('hands a system error to the error dialog and shows nothing of its own', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetch(json({status: 500}, 500));

        renderWithQueries(<MemoryRouter><SoftwareListProvider><SoftwareList/></SoftwareListProvider></MemoryRouter>);

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(reports[0]?.error.kind).toBe('server');
        expect(screen.queryByRole('alert')).not.toBeInTheDocument();
        expect(screen.queryByTestId('software-list-skeleton')).not.toBeInTheDocument();
    });
});
