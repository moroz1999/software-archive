import {describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {RatingStars} from './RatingStars';

const starLabel = (stars: number) => `${String(stars)} stars`;

/** How far the filled stars reach, as the width of their layer. */
function filled(): string | null {
    return document.querySelector('[data-rating-fill]')?.getAttribute('width') ?? null;
}

describe(RatingStars, () => {
    it('shows the rating as stars filled as far as it goes', () => {
        render(<RatingStars value={4.08} label="Rating 4.08 of 5"/>);

        expect(screen.getByRole('img', {name: 'Rating 4.08 of 5'})).toBeInTheDocument();
        expect(filled()).toBe('81.6%');
        expect(screen.queryByRole('button')).not.toBeInTheDocument();
    });

    it('is a medium set of stars by default and a small one on request', () => {
        const {rerender} = render(<RatingStars value={3} label="Rating"/>);

        expect(screen.getByRole('img', {name: 'Rating'})).toHaveClass('ratingStars', 'md');

        rerender(<RatingStars value={3} label="Rating" size="sm"/>);

        expect(screen.getByRole('img', {name: 'Rating'})).toHaveClass('ratingStars', 'sm');
    });

    it('gives as many stars as the star pressed', async () => {
        const onRate = vi.fn<(stars: number) => void>();
        render(<RatingStars value={0} label="Rating" onRate={onRate} starLabel={starLabel}/>);

        expect(screen.getByRole('group', {name: 'Rating'})).toBeInTheDocument();
        expect(screen.getAllByRole('button').map((button) => button.getAttribute('aria-label')))
            .toStrictEqual(['1 stars', '2 stars', '3 stars', '4 stars', '5 stars']);

        await userEvent.click(screen.getByRole('button', {name: '4 stars'}));

        expect(onRate).toHaveBeenCalledExactlyOnceWith(4);
    });

    it('shows what a star would give while it is pointed at, and the rating again after', async () => {
        render(<RatingStars value={4.08} label="Rating" onRate={vi.fn<(stars: number) => void>()} starLabel={starLabel}/>);

        await userEvent.hover(screen.getByRole('button', {name: '2 stars'}));

        expect(filled()).toBe('40%');

        await userEvent.unhover(screen.getByRole('button', {name: '2 stars'}));

        expect(filled()).toBe('81.6%');
    });

    it('presses the star the reader gave', () => {
        render(<RatingStars value={4.08} label="Rating" onRate={vi.fn<(stars: number) => void>()} starLabel={starLabel} chosen={4}/>);

        expect(screen.getByRole('button', {name: '4 stars'})).toHaveAttribute('aria-pressed', 'true');
        expect(screen.getByRole('button', {name: '5 stars'})).toHaveAttribute('aria-pressed', 'false');
    });

    it('gives nothing while disabled', async () => {
        const onRate = vi.fn<(stars: number) => void>();
        render(<RatingStars value={0} label="Rating" onRate={onRate} starLabel={starLabel} disabled/>);

        await userEvent.click(screen.getByRole('button', {name: '3 stars'}));

        expect(screen.getByRole('button', {name: '3 stars'})).toBeDisabled();
        expect(onRate).not.toHaveBeenCalled();
    });

    it('shows a tooltip over the stars when given one', () => {
        render(<RatingStars value={4} label="Rating 4 of 5" title="3 votes"/>);

        expect(screen.getByRole('img', {name: 'Rating 4 of 5'})).toHaveAttribute('title', '3 votes');
    });
});
