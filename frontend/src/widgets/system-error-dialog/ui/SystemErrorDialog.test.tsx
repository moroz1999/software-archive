import {describe, expect, it, vi} from 'vitest';
import {act, render, screen} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {reportSystemError, SystemError} from '../../../shared/api';
import {SystemErrorDialog} from './SystemErrorDialog';

describe(SystemErrorDialog, () => {
    it('stays hidden until a system error is reported', () => {
        render(<SystemErrorDialog/>);

        expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
    });

    it('shows a server failure and retries the failed action', async () => {
        const retry = vi.fn<() => void>();
        render(<SystemErrorDialog/>);

        act(() => {
            reportSystemError({error: new SystemError('server', 'HTTP 500'), retry});
        });

        expect(screen.getByRole('alertdialog', {name: 'Something went wrong'}))
            .toHaveTextContent('The server could not complete the request. Please try again.');

        await userEvent.click(screen.getByRole('button', {name: 'Retry'}));

        expect(retry).toHaveBeenCalledExactlyOnceWith();
        expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
    });

    it('explains a network failure', () => {
        render(<SystemErrorDialog/>);

        act(() => {
            reportSystemError({error: new SystemError('network', 'Network failure')});
        });

        expect(screen.getByRole('alertdialog')).toHaveTextContent('Check your connection and try again.');
        expect(screen.queryByRole('button', {name: 'Retry'})).not.toBeInTheDocument();
    });

    it('closes without retrying', async () => {
        const retry = vi.fn<() => void>();
        render(<SystemErrorDialog/>);
        act(() => {
            reportSystemError({error: new SystemError('response', 'Unexpected response'), retry});
        });

        await userEvent.click(screen.getByRole('button', {name: 'Close'}));

        expect(retry).not.toHaveBeenCalled();
        expect(screen.queryByRole('alertdialog')).not.toBeInTheDocument();
    });
});
