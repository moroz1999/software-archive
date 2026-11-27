import {describe, expect, it} from 'vitest';
import {render, screen, within} from '@testing-library/react';
import {MemoryRouter} from 'react-router';
import type {Software} from '../model/software';
import {SoftwareCardLayout} from './SoftwareCardLayout';

const ALPHA = {
    id: '00000000-0000-0000-0001-000000000001',
    title: 'Fixture Software Alpha',
    releaseDate: '1985-03-02',
    screenshots: [],
    madeBy: [],
    rating: 0,
    votesAmount: 0,
} satisfies Software;

describe(SoftwareCardLayout, () => {
    it('is a card named by the title, with an image placeholder for no screenshots and the release year', () => {
        render(<MemoryRouter><SoftwareCardLayout footer={null} software={ALPHA} href="/software/1/edit"/></MemoryRouter>);

        const card = screen.getByRole('article', {name: 'Fixture Software Alpha'});

        expect(within(card).getByRole('heading', {level: 3, name: 'Fixture Software Alpha'})).toBeInTheDocument();
        expect(within(card).getByRole('img', {name: 'No screenshot yet'})).toBeInTheDocument();
        expect(within(card).getByText('1985')).toBeInTheDocument();
    });

    it('shows its screenshots in place of the placeholder, the first at once', () => {
        const screenshots = [
            {id: '00000000-0000-0000-0055-000000000001', url: '/api/screenshots/1'},
            {id: '00000000-0000-0000-0055-000000000002', url: '/api/screenshots/2'},
        ];
        render(<MemoryRouter><SoftwareCardLayout footer={null} software={{...ALPHA, screenshots}} href="/software/1"/></MemoryRouter>);

        const card = screen.getByRole('article', {name: 'Fixture Software Alpha'});

        expect(within(card).getByRole('img', {name: 'Fixture Software Alpha'})).toHaveAttribute('src', '/api/screenshots/1');
        expect(within(card).queryByRole('img', {name: 'No screenshot yet'})).not.toBeInTheDocument();
    });

    it('is one link to the given page, named by the title, holding the picture and every line', () => {
        render(<MemoryRouter><SoftwareCardLayout footer={null} software={{...ALPHA, madeBy: ['Ultimate']}} href="/software/1/edit"/></MemoryRouter>);

        const link = screen.getByRole('link', {name: 'Fixture Software Alpha'});

        expect(screen.getAllByRole('link')).toHaveLength(1);
        expect(link).toHaveAttribute('href', '/software/1/edit');
        expect(within(link).getByRole('img', {name: 'No screenshot yet'})).toBeInTheDocument();
        expect(within(link).getByText('Ultimate')).toBeInTheDocument();
        expect(within(link).getByText('1985')).toBeInTheDocument();
    });

    it('names who made the software, in the order given', () => {
        render(<MemoryRouter><SoftwareCardLayout footer={null} software={{...ALPHA, madeBy: ['Ultimate', 'Zed']}} href="/software/1"/></MemoryRouter>);

        expect(screen.getByText('Ultimate, Zed')).toBeInTheDocument();
        expect(screen.getByText('1985')).toBeInTheDocument();
    });

    it('names who made the software even when the release date is unknown', () => {
        render(<MemoryRouter><SoftwareCardLayout footer={null} software={{...ALPHA, releaseDate: null, madeBy: ['Ultimate']}} href="/software/1"/></MemoryRouter>);

        expect(screen.getByText('Ultimate')).toBeInTheDocument();
        expect(screen.queryByText('1985')).not.toBeInTheDocument();
    });

    it('shows no year when the release date is unknown', () => {
        render(<MemoryRouter><SoftwareCardLayout footer={null} software={{...ALPHA, releaseDate: null}} href="/software/1/edit"/></MemoryRouter>);

        expect(screen.queryByText('1985')).not.toBeInTheDocument();
    });
});
