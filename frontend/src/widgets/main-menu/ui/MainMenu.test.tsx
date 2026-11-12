import {afterEach, describe, expect, it, vi} from 'vitest';
import {screen, within} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {PlatformSelectionProvider} from '@/entities/platforms';
import {json, renderWithQueries, stubFetch} from '@/shared/lib/test';
import {MainMenu} from './MainMenu';

function renderAt(path: string) {
    stubFetch(json([{code: 'vector', name: 'Vector'}]));
    renderWithQueries(<PlatformSelectionProvider><MemoryRouter initialEntries={[path]}><MainMenu/></MemoryRouter></PlatformSelectionProvider>);
}

describe(MainMenu, () => {
    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('holds the platform switch', async () => {
        renderAt('/');

        await expect(screen.findByRole('option', {name: 'Vector'})).resolves.toBeInTheDocument();
        expect(screen.getByRole('combobox', {name: 'Platform'})).toHaveValue('');
    });

    it('leads to the home page, the software, the authors, the groups and the categories', () => {
        renderAt('/');

        const menu = within(screen.getByRole('navigation', {name: 'Main menu'}));

        expect(menu.getByRole('link', {name: 'Home'})).toHaveAttribute('href', '/');
        expect(menu.getByRole('link', {name: 'Software'})).toHaveAttribute('href', '/software');
        expect(menu.getByRole('link', {name: 'Authors'})).toHaveAttribute('href', '/authors');
        expect(menu.getByRole('link', {name: 'Groups'})).toHaveAttribute('href', '/groups');
        expect(menu.getByRole('link', {name: 'Categories'})).toHaveAttribute('href', '/categories');
    });

    it('offers the quick search', () => {
        renderAt('/');

        expect(screen.getByRole('button', {name: 'Search'})).toBeInTheDocument();
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
        ['/categories', 'Categories'],
        ['/categories/00000000-0000-0000-0006-000000000001/edit', 'Categories'],
    ])('marks the section of %s', (path, section) => {
        renderAt(path);

        expect(screen.getByRole('link', {name: section})).toHaveAttribute('aria-current', 'page');
        expect(screen.getByRole('link', {name: 'Home'})).not.toHaveAttribute('aria-current');
    });
});
