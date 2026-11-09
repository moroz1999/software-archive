import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {NavBar, NavBarLink} from './NavBar';

function renderAt(path: string) {
    render(
        <MemoryRouter initialEntries={[path]}>
            <NavBar label="Main">
                <NavBarLink href="/" end>Home</NavBarLink>
                <NavBarLink href="/authors">Authors</NavBarLink>
            </NavBar>
        </MemoryRouter>,
    );
}

describe(NavBar, () => {
    it('is a labelled navigation with a list of links', () => {
        renderAt('/');

        expect(screen.getByRole('navigation', {name: 'Main'})).toBeInTheDocument();
        expect(screen.getAllByRole('listitem')).toHaveLength(2);
        expect(screen.getByRole('link', {name: 'Authors'})).toHaveAttribute('href', '/authors');
    });

    it('marks the link of the current section, including the pages inside it', () => {
        renderAt('/authors/42');

        expect(screen.getByRole('link', {name: 'Authors'})).toHaveAttribute('aria-current', 'page');
        expect(screen.getByRole('link', {name: 'Authors'})).toHaveClass('link', 'active');
        expect(screen.getByRole('link', {name: 'Home'})).not.toHaveAttribute('aria-current');
    });

    it('marks an end link only on its own address', () => {
        renderAt('/');

        expect(screen.getByRole('link', {name: 'Home'})).toHaveAttribute('aria-current', 'page');
        expect(screen.getByRole('link', {name: 'Authors'})).not.toHaveClass('active');
    });
});
