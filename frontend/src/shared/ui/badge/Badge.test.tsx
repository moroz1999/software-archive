import {describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {Badge} from './Badge';

describe(Badge, () => {
    it('is a primary label by default', () => {
        render(<Badge>48K</Badge>);

        expect(screen.getByText('48K')).toHaveClass('badge', 'primary');
        expect(screen.queryByRole('button')).not.toBeInTheDocument();
    });

    it('is a button when it reacts to clicks', async () => {
        const onClick = vi.fn<() => void>();
        render(<Badge variant="secondary" onClick={onClick}>48K</Badge>);

        const badge = screen.getByRole('button', {name: '48K'});
        await userEvent.click(badge);

        expect(badge).toHaveClass('secondary', 'clickable');
        expect(onClick).toHaveBeenCalledExactlyOnceWith();
    });
});
