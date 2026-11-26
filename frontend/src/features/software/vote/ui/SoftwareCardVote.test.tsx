import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {type Software, toSoftwareId, toSoftwareTitle} from '@/entities/software';
import {CurrentUserProvider} from '@/entities/users';
import {json, noContent, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {SoftwareCardVote} from './SoftwareCardVote';

const ELITE_ID = '00000000-0000-0000-0001-000000000001';
const EXOLON_ID = '00000000-0000-0000-0001-000000000002';
const ELITE: Software = {id: toSoftwareId(ELITE_ID), title: toSoftwareTitle('Elite'), releaseDate: null, rating: {rating: 4.0769, votesAmount: 3}};
const EXOLON: Software = {id: toSoftwareId(EXOLON_ID), title: toSoftwareTitle('Exolon'), releaseDate: null, rating: {rating: 0, votesAmount: 0}};
const MY_VOTES = `/api/my-votes?software=${ELITE_ID},${EXOLON_ID}`;
const MEMBER = {id: '00000000-0000-0000-0009-000000000003', name: 'Zed', email: 'zed@example.com', groups: ['member'], abilities: ['add-entries']};

function renderCards(...software: Software[]) {
    renderWithQueries(
        <CurrentUserProvider>
            {software.map((item) => <article key={item.id} aria-label={item.title}><SoftwareCardVote software={item}/></article>)}
        </CurrentUserProvider>,
    );
}

describe(SoftwareCardVote, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows a public visitor the rating of the card as stars, the number of votes in their tooltip', async () => {
        const fetchMock = stubFetchRoutes(new Map([['/api/auth/me', noContent()]]));
        renderCards(ELITE, EXOLON);

        const elite = await screen.findByRole('img', {name: 'Rating 4.08 of 5'});

        expect(elite).toHaveAttribute('title', '3 votes');
        expect(screen.getByRole('img', {name: 'Rating 0 of 5'})).toHaveAttribute('title', 'No votes yet.');
        expect(fetchMock.mock.calls.map(([url]) => url)).not.toContain(MY_VOTES);
    });

    it('asks for the reader\'s votes on every card of the list at once and shows each beside its stars', async () => {
        const fetchMock = stubFetchRoutes(new Map([
            ['/api/auth/me', json(MEMBER)],
            [MY_VOTES, json([{software: ELITE_ID, value: 4}])],
        ]));
        renderCards(ELITE, EXOLON);

        const elite = await screen.findByRole('article', {name: 'Elite'});

        await expect(within(elite).findByText('4')).resolves.toBeInTheDocument();
        expect(within(elite).getByRole('button', {name: 'Give 4 stars'})).toHaveAttribute('aria-pressed', 'true');
        expect(within(screen.getByRole('article', {name: 'Exolon'})).queryByRole('button', {name: 'Withdraw your vote'})).not.toBeInTheDocument();
        expect(fetchMock.mock.calls.filter(([url]) => typeof url === 'string' && url.startsWith('/api/my-votes'))).toHaveLength(1);
    });

    it('lets the reader vote on a card and shows the rating the vote makes', async () => {
        const fetchMock = stubFetchRoutes(new Map([
            ['/api/auth/me', json(MEMBER)],
            [`/api/my-votes?software=${EXOLON_ID}`, json([])],
            [`/api/software/${EXOLON_ID}/vote`, json({rating: 3.9091, votesAmount: 1, myVote: 5})],
        ]));
        renderCards(EXOLON);

        await userEvent.click(await screen.findByRole('button', {name: 'Give 5 stars'}));

        await expect(screen.findByText('5')).resolves.toBeInTheDocument();
        expect(screen.getByRole('group', {name: 'Rating 3.91 of 5'})).toHaveAttribute('title', '1 vote');
        expect(fetchMock.mock.calls.find(([url]) => url === `/api/software/${EXOLON_ID}/vote`)?.[1]).toMatchObject({method: 'PUT', body: '{"value":5}'});
    });

    it('lets the reader withdraw their vote with the cross', async () => {
        stubFetchRoutes(new Map([
            ['/api/auth/me', json(MEMBER)],
            [`/api/my-votes?software=${ELITE_ID}`, json([{software: ELITE_ID, value: 4}])],
            [`/api/software/${ELITE_ID}/vote`, json({rating: 4.0833, votesAmount: 2, myVote: null})],
        ]));
        renderCards(ELITE);

        await userEvent.click(await screen.findByRole('button', {name: 'Withdraw your vote'}));

        await expect(screen.findByRole('group', {name: 'Rating 4.08 of 5'})).resolves.toHaveAttribute('title', '2 votes');
        expect(screen.queryByText('4')).not.toBeInTheDocument();
    });
});
