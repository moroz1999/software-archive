import {describe, expect, it, vi} from 'vitest';
import {render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {Dialog} from './Dialog';
import {ErrorDialog} from './ErrorDialog';

describe(Dialog, () => {
    it('shows a modal dialog labelled by its title', () => {
        render(<Dialog open title="Delete" onClose={vi.fn<() => void>()}>Body</Dialog>);

        const dialog = screen.getByRole('dialog', {name: 'Delete'});

        expect(dialog).toHaveAttribute('aria-modal', 'true');
        expect(dialog).toHaveTextContent('Body');
    });

    it('renders nothing when closed', () => {
        render(<Dialog open={false} title="Delete" onClose={vi.fn<() => void>()}>Body</Dialog>);

        expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
    });

    it('closes on Escape', async () => {
        const onClose = vi.fn<() => void>();
        render(<Dialog open title="Delete" onClose={onClose}>Body</Dialog>);

        await userEvent.keyboard('{Escape}');

        expect(onClose).toHaveBeenCalledExactlyOnceWith();
    });
});

describe(ErrorDialog, () => {
    it('shows the message as an alert dialog with retry and close', async () => {
        const onRetry = vi.fn<() => void>();
        const onClose = vi.fn<() => void>();
        render(<ErrorDialog open title="Something went wrong" message="Try again." onRetry={onRetry} onClose={onClose}/>);

        expect(screen.getByRole('alertdialog', {name: 'Something went wrong'})).toHaveTextContent('Try again.');

        await userEvent.click(screen.getByRole('button', {name: 'Retry'}));
        await userEvent.click(screen.getByRole('button', {name: 'Close'}));

        expect(onRetry).toHaveBeenCalledExactlyOnceWith();
        expect(onClose).toHaveBeenCalledExactlyOnceWith();
    });

    it('offers no retry when the action cannot be repeated', () => {
        render(<ErrorDialog open title="Something went wrong" message="Try again." onClose={vi.fn<() => void>()}/>);

        expect(screen.queryByRole('button', {name: 'Retry'})).not.toBeInTheDocument();
    });
});
