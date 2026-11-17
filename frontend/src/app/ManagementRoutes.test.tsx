import {afterEach, describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import {createQueryClient} from '@/shared/api';
import {json, noContent, stubFetchRoutes} from '@/shared/lib/test';
import {App} from './App';
import {type AppRouter, createAppRouter} from './router';

const MEMBER = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed', email: 'zed@example.com', groups: ['member'], abilities: ['add-entries']};
const ADMIN = {...MEMBER, groups: ['admin', 'member'], abilities: ['add-entries', 'keep-categories']};
const BOB = '00000000-0000-0000-0009-000000000003';

describe('management routes', () => {
    let router: AppRouter | undefined;

    afterEach(() => {
        router?.dispose();
        router = undefined;
        window.history.replaceState(null, '', '/');
        vi.unstubAllGlobals();
    });

    function renderAt(path: string, user: typeof MEMBER | null) {
        stubFetchRoutes(new Map([
            ['/api/auth/me', user === null ? noContent() : json(user)],
            ['/api/platforms', json([])],
            ['/api/categories', json([])],
            ['/api/imports/vektor/sections', json(['zagruzchiki'])],
            ['/api/imports/last-run', json({run: null})],
            ['/api/users?page=0&size=50', json({content: [], page: {size: 50, number: 0, totalElements: 0, totalPages: 0}})],
            [`/api/users/${BOB}`, json({id: BOB, name: 'Bob', email: 'bob@example.com', confirmed: true, registeredAt: '2026-01-15T10:00:00Z', groups: ['member']})],
        ]));
        window.history.replaceState(null, '', path);
        const queryClient = createQueryClient();
        router = createAppRouter(queryClient);
        render(<App router={router} queryClient={queryClient}/>);
    }

    it('opens category management for an administrator', async () => {
        renderAt('/management/categories', ADMIN);

        await expect(screen.findByRole('heading', {level: 1, name: 'Categories'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('navigation', {name: 'Management'})).toBeInTheDocument();
    });

    it('hides a management address from a member', async () => {
        renderAt('/management/categories', MEMBER);

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
    });

    it('hides a management address from a visitor', async () => {
        renderAt('/management/categories', null);

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
    });

    it('opens user management for an administrator', async () => {
        renderAt('/management/users', ADMIN);

        await expect(screen.findByRole('heading', {level: 1, name: 'Users'})).resolves.toBeInTheDocument();
        await expect(screen.findByText('No users found.')).resolves.toBeInTheDocument();
    });

    it('hides user management from a member', async () => {
        renderAt('/management/users', MEMBER);

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
    });

    it('opens the editing of a user for an administrator', async () => {
        renderAt(`/management/users/${BOB}/edit`, ADMIN);

        await expect(screen.findByRole('heading', {level: 1, name: 'Edit user'})).resolves.toBeInTheDocument();
        await expect(screen.findByRole('textbox', {name: 'User name'})).resolves.toHaveValue('Bob');
    });

    it('hides the editing of a user from a member', async () => {
        renderAt(`/management/users/${BOB}/edit`, MEMBER);

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
    });

    it('opens the imports for an administrator', async () => {
        renderAt('/management/imports', ADMIN);

        await expect(screen.findByRole('heading', {level: 1, name: 'Imports'})).resolves.toBeInTheDocument();
        await expect(screen.findByRole('option', {name: 'zagruzchiki'})).resolves.toBeInTheDocument();
    });

    it('hides the imports from a member', async () => {
        renderAt('/management/imports', MEMBER);

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
    });

    it('does not keep the development category address', async () => {
        renderAt('/categories', ADMIN);

        await expect(screen.findByRole('heading', {level: 1, name: 'Not found'})).resolves.toBeInTheDocument();
    });
});
