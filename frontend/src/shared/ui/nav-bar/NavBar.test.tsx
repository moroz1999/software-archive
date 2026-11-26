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
    it('provides a separate logo image for visitors who prefer a dark theme', () => {
        render(
            <MemoryRouter>
                <NavBar label="Main" brand={{src: '/light.svg', darkSrc: '/dark.svg', alt: 'Archive', width: 95, height: 36}}>
                    <NavBarLink href="/">Home</NavBarLink>
                </NavBar>
            </MemoryRouter>,
        );

        const logo = screen.getByRole('img', {name: 'Archive'});
        const darkLogo = logo.closest('picture')?.querySelector('source[media="(prefers-color-scheme: dark)"]');

        expect(logo).toHaveAttribute('src', '/light.svg');
        expect(darkLogo).toHaveAttribute('srcset', '/dark.svg');
    });

    it('is a labelled navigation with a list of links', () => {
        renderAt('/');

        expect(screen.getByRole('navigation', {name: 'Main'})).toBeInTheDocument();
        expect(screen.getAllByRole('listitem')).toHaveLength(2);
        expect(screen.getByRole('link', {name: 'Authors'})).toHaveAttribute('href', '/authors');
    });

    it('holds controls apart from the links', () => {
        render(<MemoryRouter><NavBar label="Main" actions={<span>Switch</span>}><NavBarLink href="/">Home</NavBarLink></NavBar></MemoryRouter>);

        expect(screen.getByText('Switch')).toBeInTheDocument();
        expect(screen.getByRole('navigation', {name: 'Main'})).not.toHaveTextContent('Switch');
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
