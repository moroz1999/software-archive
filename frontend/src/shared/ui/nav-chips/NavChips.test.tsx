import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {NavChip, NavChips} from './NavChips';

function renderChips(disabled = false) {
    render(
        <MemoryRouter>
            <NavChips label="First letter">
                <NavChip href="/authors" disabled={disabled}>All</NavChip>
                <NavChip href="/authors?letter=A" active disabled={disabled}>A</NavChip>
                <NavChip href="/authors?letter=B" disabled={disabled}>B</NavChip>
            </NavChips>
        </MemoryRouter>,
    );
}

describe(NavChips, () => {
    it('is a labelled navigation with a list of link chips', () => {
        renderChips();

        expect(screen.getByRole('navigation', {name: 'First letter'})).toBeInTheDocument();
        expect(screen.getAllByRole('listitem')).toHaveLength(3);
        expect(screen.getByRole('link', {name: 'B'})).toHaveAttribute('href', '/authors?letter=B');
    });

    it('marks the active chip as current', () => {
        renderChips();

        expect(screen.getByRole('link', {name: 'A'})).toHaveAttribute('aria-current', 'page');
        expect(screen.getByRole('link', {name: 'A'})).toHaveClass('chip', 'active');
        expect(screen.getByRole('link', {name: 'B'})).not.toHaveAttribute('aria-current');
    });

    it('locks every chip while disabled', () => {
        renderChips(true);

        expect(screen.queryByRole('link')).not.toBeInTheDocument();
        expect(screen.getByText('B')).toHaveAttribute('aria-disabled', 'true');
        expect(screen.getByText('A')).toHaveAttribute('aria-current', 'page');
    });
});
