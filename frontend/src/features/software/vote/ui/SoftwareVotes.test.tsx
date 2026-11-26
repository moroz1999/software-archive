import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import {toSoftwareId} from '@/entities/software';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {SoftwareVotesProvider} from '../model/SoftwareVotesProvider';
import {SoftwareVotes} from './SoftwareVotes';

const ID = '00000000-0000-0000-0001-000000000001';
const VOTES = `/api/software/${ID}/votes`;

function renderVotes() {
    renderWithQueries(<SoftwareVotesProvider id={toSoftwareId(ID)} enabled><SoftwareVotes/></SoftwareVotesProvider>);
}

describe(SoftwareVotes, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('names who voted, with their stars and the day, as the archive orders them', async () => {
        stubFetchRoutes(new Map([[VOTES, json([
            {voter: 'Zork', value: 5, castAt: '2026-11-24T10:00:00Z'},
            {voter: 'Zed', value: 3, castAt: '2026-03-04T05:06:07Z'},
        ])]]));
        renderVotes();

        const votes = await screen.findByRole('region', {name: 'Votes'});
        const items = await within(votes).findAllByRole('listitem');

        expect(items.map((item) => item.textContent)).toStrictEqual([
            expect.stringContaining('Zork'), expect.stringContaining('Zed'),
        ]);
        expect(items[0]).toHaveTextContent('24 November 2026');
        expect(items[1]).toHaveTextContent('4 March 2026');
        expect(within(votes).getAllByRole('img').map((stars) => stars.getAttribute('aria-label'))).toStrictEqual(['5 of 5', '3 of 5']);
    });

    it('says when nobody voted yet', async () => {
        stubFetchRoutes(new Map([[VOTES, json([])]]));
        renderVotes();

        await expect(screen.findByText('Nobody has voted yet.')).resolves.toBeInTheDocument();
    });

    it('hands a failed read to the error dialog and shows no votes', async () => {
        const reports: SystemErrorReport[] = [];
        const unsubscribe = subscribeSystemErrors((report) => {
            reports.push(report);
        });
        stubFetchRoutes(new Map([[VOTES, json({status: 500}, 500)]]));
        renderVotes();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(screen.queryByRole('listitem')).not.toBeInTheDocument();

        unsubscribe();
    });
});
