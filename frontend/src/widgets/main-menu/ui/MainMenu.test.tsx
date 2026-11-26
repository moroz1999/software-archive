import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {PlatformSelectionProvider} from '@/entities/platforms';
import {CurrentUserProvider} from '@/entities/users';
import {json, noContent, renderWithQueries, stubFetchRoutes} from '@/shared/lib/test';
import {MainMenu} from './MainMenu';

const MEMBER = {id: '00000000-0000-0000-0009-000000000001', name: 'Zed', email: 'zed@example.com', groups: ['member'], abilities: ['add-entries']};
const ADMIN = {...MEMBER, groups: ['admin', 'member'], abilities: ['add-entries', 'keep-categories']};
const EDITOR = {...MEMBER, groups: ['admin', 'member', 'ai-assistant'], abilities: ['add-entries', 'keep-categories', 'use-assistant']};

function renderAt(path: string, me: () => Response = noContent()) {
    stubFetchRoutes(new Map([
        ['/api/platforms', json([{code: 'vector', name: 'Vector'}])],
        ['/api/auth/me', me],
    ]));
    renderWithQueries((
        <CurrentUserProvider>
            <PlatformSelectionProvider><MemoryRouter initialEntries={[path]}><MainMenu/></MemoryRouter></PlatformSelectionProvider>
        </CurrentUserProvider>
    ));
}

describe(MainMenu, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('shows the archive logo before the main menu in the header', () => {
        renderAt('/');

        const header = within(screen.getByRole('banner'));
        const logo = header.getByRole('img', {name: 'Ретротека'});
        const menu = header.getByRole('navigation', {name: 'Main menu'});

        expect(logo).toAppearBefore(menu);
    });

    it('holds the platform switch', async () => {
        renderAt('/');

        await expect(screen.findByRole('option', {name: 'Vector'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('combobox', {name: 'Platform'})).toHaveValue('');
    });

    it('leads to the public sections without offering management to a visitor', () => {
        renderAt('/');

        const menu = within(screen.getByRole('navigation', {name: 'Main menu'}));

        expect(menu.getByRole('link', {name: 'Home'})).toHaveAttribute('href', '/');
        expect(menu.getByRole('link', {name: 'Software'})).toHaveAttribute('href', '/software');
        expect(menu.getByRole('link', {name: 'Authors'})).toHaveAttribute('href', '/authors');
        expect(menu.getByRole('link', {name: 'Groups'})).toHaveAttribute('href', '/groups');
        expect(menu.queryByRole('link', {name: 'Management'})).not.toBeInTheDocument();
    });

    it('offers management to an administrator', async () => {
        renderAt('/', json(ADMIN));

        await expect(screen.findByRole('link', {name: 'Management'})).resolves.toHaveAttribute('href', '/management/categories');
        expect(screen.queryByRole('link', {name: 'Categories'})).not.toBeInTheDocument();
    });

    it('does not offer management to a signed-in member', async () => {
        renderAt('/', json(MEMBER));

        await vi.waitFor(() => {
            expect(screen.queryByRole('link', {name: 'Management'})).not.toBeInTheDocument();
        });
    });

    it('offers the AI assistant to a member of its group', async () => {
        renderAt('/', json(EDITOR));

        await expect(screen.findByRole('button', {name: 'AI assistant'})).resolves.toBeInTheDocument();
    });

    it('does not offer the AI assistant to an administrator outside its group, nor to a visitor', async () => {
        renderAt('/', json(ADMIN));

        await expect(screen.findByRole('link', {name: 'Management'})).resolves.toBeInTheDocument();
        expect(screen.queryByRole('button', {name: 'AI assistant'})).not.toBeInTheDocument();
    });

    it('offers the quick search', () => {
        renderAt('/');

        expect(screen.getByRole('button', {name: 'Search'})).toBeInTheDocument();
    });

    it('offers the account menu', () => {
        renderAt('/');

        expect(screen.getByRole('button', {name: 'Account'})).toBeInTheDocument();
    });

    it('marks only the home page on the home page', () => {
        renderAt('/');

        expect(screen.getByRole('link', {name: 'Home'})).toHaveAttribute('aria-current', 'page');
        expect(screen.getByRole('link', {name: 'Software'})).not.toHaveAttribute('aria-current');
    });

    it.each([
        ['/software', 'Software'],
        ['/software/00000000-0000-0000-0001-000000000001', 'Software'],
        ['/authors', 'Authors'],
        ['/authors/00000000-0000-0000-0003-000000000001', 'Authors'],
        ['/groups', 'Groups'],
        ['/groups/00000000-0000-0000-0005-000000000001', 'Groups'],
    ])('marks the section of %s', (path, section) => {
        renderAt(path);

        expect(screen.getByRole('link', {name: section})).toHaveAttribute('aria-current', 'page');
        expect(screen.getByRole('link', {name: 'Home'})).not.toHaveAttribute('aria-current');
    });

    it.each([
        '/management/categories',
        '/management/categories/00000000-0000-0000-0006-000000000001/edit',
        '/management/users',
    ])('marks management as the section of %s', async (path) => {
        renderAt(path, json(ADMIN));

        await expect(screen.findByRole('link', {name: 'Management'})).resolves.toHaveAttribute('aria-current', 'page');
        expect(screen.getByRole('link', {name: 'Home'})).not.toHaveAttribute('aria-current');
    });
});
