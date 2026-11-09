import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {MainMenu} from './MainMenu';

function renderAt(path: string) {
    render(<MemoryRouter initialEntries={[path]}><MainMenu/></MemoryRouter>);
}

describe(MainMenu, () => {
    it('leads to the home page, the software and the authors', () => {
        renderAt('/');

        const menu = screen.getByRole('navigation', {name: 'Main menu'});

        expect(menu).toBeInTheDocument();
        expect(screen.getByRole('link', {name: 'Home'})).toHaveAttribute('href', '/');
        expect(screen.getByRole('link', {name: 'Software'})).toHaveAttribute('href', '/software');
        expect(screen.getByRole('link', {name: 'Authors'})).toHaveAttribute('href', '/authors');
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
    ])('marks the section of %s', (path, section) => {
        renderAt(path);

        expect(screen.getByRole('link', {name: section})).toHaveAttribute('aria-current', 'page');
        expect(screen.getByRole('link', {name: 'Home'})).not.toHaveAttribute('aria-current');
    });
});
