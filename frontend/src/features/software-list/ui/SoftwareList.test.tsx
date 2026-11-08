import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {subscribeSystemErrors, type SystemErrorReport} from '../../../shared/api';
import {json, pageResponse, stubFetch} from '../../../shared/lib/test';
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

        render(<MemoryRouter><SoftwareList/></MemoryRouter>);

        expect(screen.getByTestId('software-list-skeleton')).toBeInTheDocument();
        await expect(screen.findByText('Fixture Software Alpha')).resolves.toBeInTheDocument();
        expect(screen.queryByTestId('software-list-skeleton')).not.toBeInTheDocument();
    });

    it('lists the software as cards', async () => {
        stubFetch(json(pageResponse([ALPHA, BETA])));

        render(<MemoryRouter><SoftwareList/></MemoryRouter>);

        await expect(screen.findByRole('article', {name: 'Fixture Software Alpha'})).resolves.toHaveTextContent('1985');
        expect(screen.getByRole('article', {name: 'Fixture Software Beta'})).toBeInTheDocument();
        expect(screen.getAllByRole('listitem')).toHaveLength(2);
    });

    it('says so when the archive has no software', async () => {
        stubFetch(json(pageResponse([])));

        render(<MemoryRouter><SoftwareList/></MemoryRouter>);

        await expect(screen.findByText('No software in the archive yet.')).resolves.toBeInTheDocument();
    });

    it('shows a business error inline and retries from there', async () => {
        stubFetch(json({status: 404, code: 'software.not-found'}, 404), json(pageResponse([ALPHA])));

        render(<MemoryRouter><SoftwareList/></MemoryRouter>);

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('This item no longer exists.');

        await userEvent.click(screen.getByRole('button', {name: 'Retry'}));

        await expect(screen.findByText('Fixture Software Alpha')).resolves.toBeInTheDocument();
        expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    });

    it('hands a system error to the error dialog and shows nothing of its own', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetch(json({status: 500}, 500));

        render(<MemoryRouter><SoftwareList/></MemoryRouter>);

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(reports[0]?.error.kind).toBe('server');
        expect(screen.queryByRole('alert')).not.toBeInTheDocument();
        expect(screen.queryByTestId('software-list-skeleton')).not.toBeInTheDocument();
    });
});
