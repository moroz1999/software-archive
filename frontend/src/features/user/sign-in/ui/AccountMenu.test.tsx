import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {CurrentUserProvider} from '@/entities/users';
import {subscribeSystemErrors, type SystemErrorReport} from '@/shared/api';
import {json, noContent, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {AccountMenu} from './AccountMenu';

const ZED = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed', email: 'zed@example.com', groups: ['member'], abilities: ['add-entries']};

function show(routes: Map<string, () => Response>) {
    const fetchMock = stubFetchRoutes(routes);
    renderWithQueries(<CurrentUserProvider><MemoryRouter><AccountMenu/></MemoryRouter></CurrentUserProvider>);
    return fetchMock;
}

/** A visitor's browser: nobody is signed in. */
function visitor(more: [string, () => Response][] = []) {
    return new Map<string, () => Response>([['/api/auth/me', noContent()], ...more]);
}

/** Opens the account window; the queries of the tests then find what is in it, the only window there is. */
async function open() {
    await userEvent.click(screen.getByRole('button', {name: 'Account'}));
    await screen.findByRole('dialog', {name: 'Account'});
}

async function signIn(login = 'Zed', password = 'password1') {
    await userEvent.type(screen.getByRole('textbox', {name: 'User name or email'}), login);
    await userEvent.type(screen.getByLabelText('Password'), password);
    await userEvent.click(screen.getByRole('button', {name: 'Sign in'}));
}

function posts(fetchMock: ReturnType<typeof stubFetchRoutes>, path: string) {
    return fetchMock.mock.calls.filter(([input, init]) => input === path && init?.method === 'POST');
}

describe(AccountMenu, () => {
    const unsubscribers: (() => void)[] = [];

    afterEach(() => {
        vi.unstubAllGlobals();
        unsubscribers.forEach((unsubscribe) => {
            unsubscribe();
        });
        unsubscribers.length = 0;
    });

    it('offers a visitor to sign in or to register', async () => {
        show(visitor());

        await open();

        expect(screen.getByRole('textbox', {name: 'User name or email'})).toHaveAttribute('autocomplete', 'username');
        expect(screen.getByLabelText('Password')).toHaveAttribute('type', 'password');
        expect(screen.getByRole('checkbox', {name: 'Remember me'})).toBeChecked();
        expect(screen.getByRole('button', {name: 'Sign in'})).toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Register'})).toHaveAttribute('href', '/register');
    });

    it('signs in and then shows who is signed in', async () => {
        const routes = visitor([['/api/auth/login', noContent()]]);
        const fetchMock = show(routes);
        await open();
        routes.set('/api/auth/me', json(ZED));

        await userEvent.click(screen.getByRole('checkbox', {name: 'Remember me'}));
        await signIn();

        await expect(screen.findByText('Zed')).resolves.toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Sign out'})).toBeInTheDocument();
        expect(posts(fetchMock, '/api/auth/login')[0]?.[1]?.body).toBe('{"login":"Zed","password":"password1","remember":false}');
    });

    it('sends nothing without a login or with a too short password, and says so at each', async () => {
        const fetchMock = show(visitor());
        await open();

        await userEvent.type(screen.getByLabelText('Password'), 'short');
        await userEvent.click(screen.getByRole('button', {name: 'Sign in'}));

        expect(screen.getByRole('textbox', {name: 'User name or email'})).toHaveAccessibleDescription('Enter your user name or email.');
        expect(screen.getByLabelText('Password')).toHaveAccessibleDescription('Use at least 8 characters.');
        expect(posts(fetchMock, '/api/auth/login')).toHaveLength(0);
    });

    it('says when the login or the password is wrong', async () => {
        show(visitor([['/api/auth/login', json({status: 422, code: 'user.credentials-invalid'}, 422)]]));
        await open();

        await signIn();

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('Wrong user name, email or password.');
    });

    it('offers an unconfirmed user a new link, sent with what they typed', async () => {
        const fetchMock = show(visitor([
            ['/api/auth/login', json({status: 422, code: 'user.unconfirmed'}, 422)],
            ['/api/auth/resend-confirmation', noContent()],
        ]));
        await open();

        await signIn();

        await expect(screen.findByRole('alert')).resolves.toHaveTextContent('Your account is not confirmed yet: open the link from the email we sent.');

        await userEvent.click(screen.getByRole('button', {name: 'Send the link again'}));

        await expect(screen.findByText('A new link is on its way to your email.')).resolves.toBeInTheDocument();
        expect(posts(fetchMock, '/api/auth/resend-confirmation')[0]?.[1]?.body).toBe('{"login":"Zed","password":"password1","language":"en"}');
    });

    it('signs a user out', async () => {
        const routes = new Map([['/api/auth/me', json(ZED)], ['/api/auth/logout', noContent()]]);
        const fetchMock = show(routes);

        await open();
        await screen.findByText('Zed');
        await userEvent.click(screen.getByRole('button', {name: 'Sign out'}));

        await expect(screen.findByRole('button', {name: 'Sign in'})).resolves.toBeInTheDocument();
        expect(posts(fetchMock, '/api/auth/logout')).toHaveLength(1);
    });

    it('hands a system error to the error dialog and keeps the form', async () => {
        const reports: SystemErrorReport[] = [];
        unsubscribers.push(subscribeSystemErrors((report) => {
            reports.push(report);
        }));
        show(visitor([['/api/auth/login', json({status: 500}, 500)]]));
        await open();

        await signIn();

        await vi.waitFor(() => {
            expect(reports).toHaveLength(1);
        });

        expect(screen.getByRole('textbox', {name: 'User name or email'})).toHaveValue('Zed');
    });
});
