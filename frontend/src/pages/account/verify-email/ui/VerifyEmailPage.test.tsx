import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {CurrentUserProvider} from '@/entities/users';
import {json, noContent, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {loadVerifyEmailPage} from '../index';

describe('VerifyEmailPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('confirms the account of the token in its address', async () => {
        const fetchMock = stubFetchRoutes(new Map([
            ['/api/auth/me', noContent()],
            ['/api/auth/confirm', json({accessToken: 'access-jwt'})],
        ]));
        const router = createMemoryRouter([
            {path: '/verify-email', lazy: {Component: loadVerifyEmailPage}, HydrateFallback: () => null},
        ], {initialEntries: ['/verify-email?token=secret']});
        renderWithQueries(<CurrentUserProvider><RouterProvider router={router}/></CurrentUserProvider>);

        await expect(screen.findByRole('heading', {level: 1, name: 'Confirm registration'})).resolves.toBeInTheDocument();
        await expect(screen.findByText('Your account is confirmed, and you are signed in.')).resolves.toBeInTheDocument();
        expect(fetchMock.mock.calls.find(([input]) => input === '/api/auth/confirm')?.[1]?.body).toBe('{"token":"secret"}');
    });
});
