import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import i18n from 'i18next';
import {toSoftwareId} from '@/entities/software';
import {CurrentUserProvider} from '@/entities/users';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {SoftwareRatingProvider} from '../model/SoftwareRatingProvider';
import {SoftwareRating} from './SoftwareRating';

const ID = '00000000-0000-0000-0001-000000000001';
const RATING = `/api/software/${ID}/rating`;
const VOTE = `/api/software/${ID}/vote`;
const MEMBER = {id: '00000000-0000-0000-0009-000000000003', name: 'Zed', email: 'zed@example.com', groups: ['member'], abilities: ['add-entries']};

/** Answers the rating, the vote and who reads them: the member unless told otherwise. */
function stubFetch(rating: () => Response, vote: () => Response = json({}), me: () => Response = json(MEMBER)) {
    return stubFetchRoutes(new Map([
        [RATING, rating], [VOTE, vote], ['/api/auth/me', me],
        // A session that cannot be refreshed: a vote refused for want of a sign-in stays refused.
        ['/api/auth/refresh', json({status: 401, code: 'auth.required'}, 401)],
    ]));
}

function renderRating() {
    renderWithQueries(
        <CurrentUserProvider>
            <SoftwareRatingProvider id={toSoftwareId(ID)}><SoftwareRating/></SoftwareRatingProvider>
        </CurrentUserProvider>,
    );
}

/** The body of the vote sent, as JSON. */
function sentVote(fetchMock: ReturnType<typeof stubFetch>): unknown {
    const init = fetchMock.mock.calls.find(([url]) => url === VOTE)?.[1];

    expect(init).toMatchObject({method: 'PUT'});

    const body = init?.body;
    return typeof body === 'string' ? JSON.parse(body) : undefined;
}

describe(SoftwareRating, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(async () => {
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
        await i18n.changeLanguage('en');
    });

    it('shows the rating of the software as stars alone, the number of votes behind it in their tooltip', async () => {
        stubFetch(json({rating: 4.0769, votesAmount: 3, myVote: null}), json({}), noContent());
        renderRating();

        const stars = await screen.findByRole('img', {name: 'Rating 4.08 of 5'});

        expect(stars).toHaveAttribute('title', '3 votes');
        expect(screen.queryByText(/4\.08/u)).not.toBeInTheDocument();
    });

    it('offers a public visitor no stars to give', async () => {
        stubFetch(json({rating: 4.0769, votesAmount: 3, myVote: null}), json({}), noContent());
        renderRating();

        await screen.findByRole('img', {name: 'Rating 4.08 of 5'});

        expect(screen.queryByRole('button', {name: 'Give 5 stars'})).not.toBeInTheDocument();
    });

    it('says when nobody voted yet', async () => {
        stubFetch(json({rating: 0, votesAmount: 0, myVote: null}));
        renderRating();

        await expect(screen.findByRole('group', {name: 'Rating 0 of 5'})).resolves.toHaveAttribute('title', 'No votes yet.');
    });

    it('lets a signed-in reader give stars and shows the rating they make', async () => {
        const fetchMock = stubFetch(json({rating: 0, votesAmount: 0, myVote: null}), json({rating: 3.8182, votesAmount: 1, myVote: 4}));
        renderRating();

        await userEvent.click(await screen.findByRole('button', {name: 'Give 4 stars'}));

        await expect(screen.findByText('4')).resolves.toBeInTheDocument();
        expect(sentVote(fetchMock)).toStrictEqual({value: 4});
        expect(screen.getByRole('group', {name: 'Rating 3.82 of 5'})).toHaveAttribute('title', '1 vote');
        expect(screen.getByRole('button', {name: 'Give 4 stars'})).toHaveAttribute('aria-pressed', 'true');
    });

    it('lets the reader withdraw their vote', async () => {
        const fetchMock = stubFetch(json({rating: 3.8182, votesAmount: 1, myVote: 4}), json({rating: 0, votesAmount: 0, myVote: null}));
        renderRating();

        await userEvent.click(await screen.findByRole('button', {name: 'Withdraw your vote'}));

        await expect(screen.findByRole('group', {name: 'Rating 0 of 5'})).resolves.toHaveAttribute('title', 'No votes yet.');
        expect(sentVote(fetchMock)).toStrictEqual({value: 0});
        expect(screen.queryByText('4')).not.toBeInTheDocument();
        expect(screen.queryByRole('button', {name: 'Withdraw your vote'})).not.toBeInTheDocument();
    });

    it('tells the reader to sign in again when the archive no longer knows them, not in the error dialog', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetch(json({rating: 0, votesAmount: 0, myVote: null}), json({status: 401, code: 'auth.required'}, 401));
        renderRating();

        await userEvent.click(await screen.findByRole('button', {name: 'Give 5 stars'}));

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('Sign in to vote.');
        expect(reports).toHaveLength(0);
    });

    it('hands a failed vote to the error dialog', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        stubFetch(json({rating: 0, votesAmount: 0, myVote: null}), json({status: 500}, 500));
        renderRating();

        await userEvent.click(await screen.findByRole('button', {name: 'Give 5 stars'}));

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(screen.queryByRole('alert')).not.toBeInTheDocument();
    });

    it('speaks Russian', async () => {
        await i18n.changeLanguage('ru');
        stubFetch(json({rating: 4.0769, votesAmount: 3, myVote: 5}));
        renderRating();

        const rating = await screen.findByRole('group', {name: 'Рейтинг 4,08 из 5'});

        expect(within(rating).getByRole('button', {name: 'Поставить 5 звёзд'})).toBeInTheDocument();
        expect(rating).toHaveAttribute('title', '3 голоса');
        expect(screen.getByText('5')).toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Снять оценку'})).toBeInTheDocument();
    });
});
