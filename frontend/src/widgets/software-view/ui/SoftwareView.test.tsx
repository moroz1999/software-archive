import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {toSoftwareId} from '@/entities/software';
import {SoftwareView} from './SoftwareView';

const ID = '00000000-0000-0000-0001-000000000001';
const ELITE = {id: ID, title: 'Elite', description: 'Space trading\nand combat', releaseDate: '1984-09-20'};

function renderView(id: string) {
    renderWithQueries(<MemoryRouter><SoftwareView id={toSoftwareId(id)}/></MemoryRouter>);
}

describe(SoftwareView, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('shows a skeleton until the software arrives', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        expect(screen.getByTestId('software-view-skeleton')).toBeInTheDocument();
        await expect(screen.findByRole('heading', {level: 1, name: 'Elite'})).resolves.toBeInTheDocument();
        expect(screen.queryByTestId('software-view-skeleton')).not.toBeInTheDocument();
    });

    it('shows the software under its title, its description line by line', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        await expect(screen.findByRole('heading', {level: 1, name: 'Elite'})).resolves.toBeInTheDocument();
        expect(screen.getByText('20 September 1984')).toBeInTheDocument();
        expect(screen.getByText('Space trading')).toBeInTheDocument();
        expect(screen.getByText('and combat')).toBeInTheDocument();
    });

    it('leads to the editing of the software', async () => {
        stubFetch(json(ELITE));
        renderView(ID);

        await expect(screen.findByRole('link', {name: 'Edit'})).resolves.toHaveAttribute('href', `/software/${ID}/edit`);
    });

    it('shows nothing for an unknown description and release date', async () => {
        stubFetch(json({...ELITE, description: null, releaseDate: null}));
        renderView(ID);

        await screen.findByRole('heading', {level: 1, name: 'Elite'});

        expect(screen.queryByText('Released')).not.toBeInTheDocument();
    });

    it('hands a business error to the error dialog and shows nothing of its own', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetch(json({status: 409, code: 'software.conflict'}, 409));
        renderView(ID);

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(screen.queryByRole('alert')).not.toBeInTheDocument();
        expect(screen.queryByRole('button', {name: 'Retry'})).not.toBeInTheDocument();
    });

    it('hands a system error to the error dialog and shows nothing of its own', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetch(json({status: 500}, 500));
        renderView(ID);

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(reports[0]?.error.kind).toBe('server');
        expect(screen.queryByTestId('software-view-skeleton')).not.toBeInTheDocument();
        expect(screen.queryByRole('link', {name: 'Edit'})).not.toBeInTheDocument();
    });
});
