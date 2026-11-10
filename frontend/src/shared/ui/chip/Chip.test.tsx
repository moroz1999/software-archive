import {describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {MemoryRouter} from 'react-router';
import {Chip} from './Chip';

describe(Chip, () => {
    it('is a medium opaque neutral chip by default', () => {
        render(<Chip>Game</Chip>);

        expect(screen.getByText('Game')).toHaveClass('chip', 'opaque', 'neutral', 'md');
    });

    it('applies variant, color and size', () => {
        render(<Chip variant="mono-outline" color="primary" size="sm">TAP</Chip>);

        expect(screen.getByText('TAP')).toHaveClass('monoOutline', 'primary', 'sm');
    });

    it('is an in-app link for an internal href', () => {
        render(<MemoryRouter><Chip href="/software?category=game">Game</Chip></MemoryRouter>);

        expect(screen.getByRole('link', {name: 'Game'})).toHaveAttribute('href', '/software?category=game');
    });

    it('is a plain link for an external href', () => {
        render(<Chip href="https://example.com">Site</Chip>);

        expect(screen.getByRole('link', {name: 'Site'})).toHaveAttribute('href', 'https://example.com');
    });

    it('can be removed by its own button', async () => {
        const onRemove = vi.fn<() => void>();
        render(<Chip onRemove={onRemove}>AY-3-8910</Chip>);

        await userEvent.click(screen.getByRole('button', {name: 'Remove AY-3-8910'}));

        expect(onRemove).toHaveBeenCalledExactlyOnceWith();
    });

    it('names its remove button by the given text', () => {
        render(<Chip onRemove={vi.fn<() => void>()} removeLabel="Drop the AY">AY-3-8910</Chip>);

        expect(screen.getByRole('button', {name: 'Drop the AY'})).toBeInTheDocument();
    });

    it('can lock its remove button', () => {
        render(<Chip onRemove={vi.fn<() => void>()} disabled>AY-3-8910</Chip>);

        expect(screen.getByRole('button', {name: 'Remove AY-3-8910'})).toBeDisabled();
    });
});
