import {describe, expect, it} from 'vitest';
import {render, screen, within} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import {toReleaseDate, toSoftwareId, toSoftwareTitle} from '../model/software';
import {SoftwareCard} from './SoftwareCard';

const ALPHA = {
    id: toSoftwareId('00000000-0000-0000-0001-000000000001'),
    title: toSoftwareTitle('Fixture Software Alpha'),
    releaseDate: toReleaseDate('1985-03-02'),
};

describe(SoftwareCard, () => {
    it('is a card named by the title, with an image placeholder and the release year', () => {
        render(<MemoryRouter><SoftwareCard software={ALPHA} href="/software/1/edit"/></MemoryRouter>);

        const card = screen.getByRole('article', {name: 'Fixture Software Alpha'});

        expect(within(card).getByRole('heading', {level: 3, name: 'Fixture Software Alpha'})).toBeInTheDocument();
        expect(within(card).getByRole('img', {name: 'No screenshot yet'})).toBeInTheDocument();
        expect(within(card).getByText('1985')).toBeInTheDocument();
    });

    it('leads by its title to the given page', () => {
        render(<MemoryRouter><SoftwareCard software={ALPHA} href="/software/1/edit"/></MemoryRouter>);

        expect(screen.getByRole('link', {name: 'Fixture Software Alpha'})).toHaveAttribute('href', '/software/1/edit');
    });

    it('shows no year when the release date is unknown', () => {
        render(<MemoryRouter><SoftwareCard software={{...ALPHA, releaseDate: null}} href="/software/1/edit"/></MemoryRouter>);

        expect(screen.queryByText('1985')).not.toBeInTheDocument();
    });
});
