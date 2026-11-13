import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen} from '@testing-library/react';
import {createMemoryRouter, RouterProvider} from 'react-router';
import {renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {loadRegisterPage} from '../index';

describe('RegisterPage', () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('holds the registration form', async () => {
        stubFetchRoutes(new Map());
        const router = createMemoryRouter([
            {path: '/register', lazy: {Component: loadRegisterPage}, HydrateFallback: () => null},
        ], {initialEntries: ['/register']});
        renderWithQueries(<RouterProvider router={router}/>);

        await expect(screen.findByRole('heading', {level: 1, name: 'Registration'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('textbox', {name: 'User name'})).toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Register'})).toBeInTheDocument();
    });
});
