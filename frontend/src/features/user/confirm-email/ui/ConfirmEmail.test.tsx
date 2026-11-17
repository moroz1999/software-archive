import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {CurrentUserProvider, useCurrentUser} from '@/entities/users';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {ConfirmEmail} from './ConfirmEmail';

const ZED = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed', email: 'zed@example.com', groups: ['member'], abilities: ['add-entries']};

function Who() {
    const {user} = useCurrentUser();
    return <p>Signed in: {user?.name ?? 'nobody'}</p>;
}

function show(token: string | null, confirmed: () => Response) {
    const routes = new Map([['/api/auth/me', noContent()], ['/api/auth/confirm', () => {
        routes.set('/api/auth/me', json(ZED));
        return confirmed();
    }]]);
    const fetchMock = stubFetchRoutes(routes);
    renderWithQueries(<CurrentUserProvider><MemoryRouter><ConfirmEmail token={token}/><Who/></MemoryRouter></CurrentUserProvider>);
    return fetchMock;
}

function confirmations(fetchMock: ReturnType<typeof stubFetchRoutes>) {
    return fetchMock.mock.calls.filter(([input]) => input === '/api/auth/confirm');
}

describe(ConfirmEmail, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('confirms the account and signs its user in', async () => {
        show('secret', json({accessToken: 'access-jwt'}));

        expect(screen.getByText('Confirming your registration…')).toBeInTheDocument();
        await expect(screen.findByText('Your account is confirmed, and you are signed in.')).resolves.toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Go to the home page'})).toHaveAttribute('href', '/');
        await expect(screen.findByText('Signed in: Zed')).resolves.toBeInTheDocument();
    });

    it('sends the token of the link once', async () => {
        const fetchMock = show('secret', json({accessToken: 'access-jwt'}));

        await screen.findByText('Your account is confirmed, and you are signed in.');

        expect(confirmations(fetchMock)).toHaveLength(1);
        expect(confirmations(fetchMock)[0]?.[1]?.body).toBe('{"token":"secret"}');
    });

    it('says when the link has expired and offers to register again', async () => {
        show('secret', json({status: 422, code: 'user.confirmation-expired'}, 422));

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('This link has expired. Register again, or sign in to get a new link.');
        expect(screen.getByRole('link', {name: 'Register again'})).toHaveAttribute('href', '/register');
    });

    it('says when the link does not work', async () => {
        show('secret', json({status: 422, code: 'user.confirmation-invalid'}, 422));

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('This link does not work. It may have been used already.');
    });

    it('sends nothing for a link without a token', async () => {
        const fetchMock = show(null, noContent());

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('This link does not work. It may have been used already.');
        expect(confirmations(fetchMock)).toHaveLength(0);
    });

    it('hands a system error to the error dialog', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        show('secret', json({status: 500}, 500));

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });
    });
});
